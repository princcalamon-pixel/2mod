
package com.suveren.feudal.entity.ai;

import com.suveren.feudal.entity.FeudalKnightEntity;
import com.suveren.feudal.util.CrownHelper;
import com.suveren.feudal.util.StockpileHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class KnightGoals {

    public static Goal openGate(FeudalKnightEntity k) { return new OpenGateGoal(k); }
    public static Goal stockpile(FeudalKnightEntity k) { return new StockpileGoal(k); }
    public static Goal stance(FeudalKnightEntity k) { return new StanceGoal(k); }

    /** Cuvaj prepozna kronanega kralja in mu odpre vrata/vrata ograje/padalo. */
    static class OpenGateGoal extends Goal {
        private final FeudalKnightEntity k;
        private final List<BlockPos> opened = new ArrayList<>();

        OpenGateGoal(FeudalKnightEntity k) { this.k = k; }

        @Override
        public boolean canUse() {
            Player o = k.getOwner();
            return o != null && CrownHelper.isCrowned(o) && k.distanceTo(o) < 8;
        }

        @Override
        public void tick() {
            Player o = k.getOwner();
            if (o == null) return;
            BlockPos c = k.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-4, -1, -4), c.offset(4, 3, 4))) {
                BlockState s = k.level().getBlockState(p);
                Block b = s.getBlock();
                boolean gate = b instanceof DoorBlock || b instanceof TrapDoorBlock || b instanceof FenceGateBlock;
                if (gate && s.hasProperty(BlockStateProperties.OPEN) && !s.getValue(BlockStateProperties.OPEN)) {
                    k.level().setBlock(p, s.setValue(BlockStateProperties.OPEN, true), 3);
                    if (b instanceof DoorBlock && s.hasProperty(DoorBlock.HALF)) {
                        BlockPos other = s.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? p.above() : p.below();
                        BlockState os = k.level().getBlockState(other);
                        if (os.hasProperty(BlockStateProperties.OPEN)) {
                            k.level().setBlock(other, os.setValue(BlockStateProperties.OPEN, true), 3);
                        }
                    }
                    if (!opened.contains(p)) opened.add(p.immutable());
                }
            }
            if (k.distanceTo(o) > 10 && !opened.isEmpty()) {
                for (BlockPos p : opened) {
                    BlockState s = k.level().getBlockState(p);
                    if (s.hasProperty(BlockStateProperties.OPEN) && s.getValue(BlockStateProperties.OPEN)) {
                        k.level().setBlock(p, s.setValue(BlockStateProperties.OPEN, false), 3);
                    }
                }
                opened.clear();
            }
        }
    }

    /** Lakota -> zitnica; poskodovan/mec manjka -> orozarna. */
    static class StockpileGoal extends Goal {
        private final FeudalKnightEntity k;
        private BlockPos target;
        private boolean armoryMode;

        StockpileGoal(FeudalKnightEntity k) { this.k = k; this.setFlags(EnumSet.of(Goal.Flag.MOVE)); }

        @Override
        public boolean canUse() {
            if (!(k.level() instanceof ServerLevel sl)) return false;
            UUID owner = k.getOwnerUUID().orElse(null);
            if (k.getFood() < 700) {
                BlockPos g = StockpileHelper.findGranary(sl, owner, k.blockPosition(), 96);
                if (g != null) { target = g; armoryMode = false; return true; }
            }
            if (needsWeapon()) {
                BlockPos a = StockpileHelper.findArmory(sl, owner, k.blockPosition(), 96);
                if (a != null) { target = a; armoryMode = true; return true; }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && k.level().getBlockEntity(target) != null;
        }

        private boolean needsWeapon() {
            ItemStack mh = k.getMainHandItem();
            if (!(mh.getItem() instanceof SwordItem)) return true;
            return mh.isDamageableItem() && mh.getDamageValue() >= mh.getMaxDamage() - 8;
        }

        @Override
        public void tick() {
            if (target == null) return;
            k.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 1.0);
            if (k.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) < 7) {
                if (k.level() instanceof ServerLevel sl) {
                    Container c = StockpileHelper.getContainer(sl, target);
                    if (c != null) {
                        if (armoryMode) {
                            ItemStack sword = StockpileHelper.takeBestSword(sl, c);
                            if (sword != null) k.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, sword);
                        } else {
                            k.addFood(StockpileHelper.eat(c));
                        }
                    }
                }
                target = null;
            }
        }
    }

    /** Drizni nacini: SLEDI / STRAZA / OSTANI / PATROLJA. */
    static class StanceGoal extends Goal {
        private final FeudalKnightEntity k;
        StanceGoal(FeudalKnightEntity k) { this.k = k; }

        @Override
        public boolean canUse() { return true; }

        @Override
        public void tick() {
            FeudalKnightEntity.Stance s = k.getStance();
            if (s == FeudalKnightEntity.Stance.FOLLOW) {
                Player o = k.getOwner();
                if (o != null && CrownHelper.isCrowned(o)) {
                    if (k.distanceTo(o) > 3.5) {
                        k.travelMove(o.getX(), o.getY(), o.getZ(), 1.25, 3.0);
                    } else {
                        k.getNavigation().stop();
                    }
                }
                return;
            }
            BlockPos post = k.getGuardPos() != null ? k.getGuardPos() : k.blockPosition();
            if (s == FeudalKnightEntity.Stance.GUARD) {
                k.travelMove(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 1.0, 2.0);
            } else if (s == FeudalKnightEntity.Stance.PATROL) {
                if (k.getNavigation().isDone() && k.tickCount % 60 == 0) {
                    Vec3 r = DefaultRandomPos.getPos(k, 10, 3);
                    if (r != null) k.travelMove(r.x, r.y, r.z, 0.9, 1.5);
                }
            }
            // HOLD: stoji na mestu.
        }
    }
}
