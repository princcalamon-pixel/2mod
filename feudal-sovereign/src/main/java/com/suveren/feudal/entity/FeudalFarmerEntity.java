
package com.suveren.feudal.entity;

import com.suveren.feudal.util.StockpileHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FeudalFarmerEntity extends SubjectEntity {
    private BlockPos home;
    private int wheatCount = 0;
    private int seedCount = 8;
    private int workCooldown = 0;
    private boolean depositing = false;
    private boolean seekingFood = false;

    public FeudalFarmerEntity(EntityType<? extends FeudalFarmerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SubjectEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public void setHome(BlockPos p) { this.home = p.immutable(); }
    public BlockPos getHome() { return home != null ? home : this.blockPosition(); }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel sl)) return;
        long day = sl.getDayTime() % 24000;
        boolean night = day >= 13000 && day <= 23000;

        if (night) {
            // Spanec doma + "zzz" delci.
            travelMove(getHome().getX() + 0.5, getHome().getY(), getHome().getZ() + 0.5, 1.0, 1.5);
            if (this.distanceToSqr(getHome().getX() + 0.5, getHome().getY(), getHome().getZ() + 0.5) < 9
                    && this.tickCount % 60 == 0) {
                sl.sendParticles(ParticleTypes.ASH,
                        getX(), getEyeY() + 0.3, getZ(), 2, 0.2, 0.1, 0.2, 0.01);
            }
            return;
        }

        // 1) Oddaja dajatve (8 psenic) v zitnico.
        if (depositing || wheatCount >= 8) {
            BlockPos g = StockpileHelper.findGranary(sl, getOwnerUUID().orElse(null), this.blockPosition(), 96);
            if (g == null) { depositing = false; return; }
            depositing = true;
            this.getNavigation().moveTo(g.getX() + 0.5, g.getY(), g.getZ() + 0.5, 1.0);
            if (this.distanceToSqr(g.getX() + 0.5, g.getY(), g.getZ() + 0.5) < 7) {
                Container c = StockpileHelper.getContainer(sl, g);
                if (c != null) {
                    ItemStack remaining = c.addItem(new ItemStack(Items.WHEAT, wheatCount));
                    wheatCount = remaining.getCount();
                }
                if (wheatCount == 0) depositing = false;
            }
            return;
        }

        // 2) Je, ce je lačen.
        if (seekingFood || food < 600) {
            BlockPos g = StockpileHelper.findGranary(sl, getOwnerUUID().orElse(null), this.blockPosition(), 96);
            if (g == null) return;
            seekingFood = true;
            this.getNavigation().moveTo(g.getX() + 0.5, g.getY(), g.getZ() + 0.5, 1.0);
            if (this.distanceToSqr(g.getX() + 0.5, g.getY(), g.getZ() + 0.5) < 7) {
                Container c = StockpileHelper.getContainer(sl, g);
                if (c != null) addFood(StockpileHelper.eat(c));
                seekingFood = false;
            }
            return;
        }

        // 3) Delo na polju.
        if (--workCooldown > 0) return;
        workCooldown = 14;

        BlockPos center = this.blockPosition();
        // Obira zrelo psenico.
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-9, -3, -9), center.offset(9, 3, 9))) {
            BlockState s = sl.getBlockState(p);
            if (s.is(Blocks.WHEAT) && s.getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7) {
                sl.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                wheatCount++;
                if (this.random.nextInt(2) == 0 && seedCount < 64) seedCount++;
                return;
            }
        }
        // Saje prazno obdelano zemljisce.
        if (seedCount > 0) {
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-9, -3, -9), center.offset(9, 3, 9))) {
                if (sl.isEmptyBlock(p) && sl.getBlockState(p.below()).is(Blocks.FARMLAND)) {
                    sl.setBlock(p, Blocks.WHEAT.defaultBlockState(), 3);
                    seedCount--;
                    return;
                }
            }
        }
        // Dolgcas: malo se sprehodi okrog doma.
        if (this.getNavigation().isDone() && this.random.nextFloat() < 0.02f) {
            int dx = this.random.nextInt(13) - 6;
            int dz = this.random.nextInt(13) - 6;
            this.getNavigation().moveTo(getHome().getX() + dx, getHome().getY(), getHome().getZ() + dz, 0.8);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putInt("Wheat", wheatCount);
        t.putInt("Seeds", seedCount);
        if (home != null) t.putLong("Home", home.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        wheatCount = t.getInt("Wheat");
        seedCount = t.getInt("Seeds");
        if (t.contains("Home")) home = BlockPos.of(t.getLong("Home"));
    }
}
