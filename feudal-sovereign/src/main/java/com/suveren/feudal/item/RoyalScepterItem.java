
package com.suveren.feudal.item;

import com.suveren.feudal.entity.FeudalKnightEntity;
import com.suveren.feudal.util.CrownHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public class RoyalScepterItem extends Item {

    public RoyalScepterItem(Properties props) { super(props); }

    /** Desni klik na vascana = novačitev v viteza. Desni klik na viteza = menjava driznega nacina. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity target, net.minecraft.world.InteractionHand hand) {
        Level level = player.level();
        if (target instanceof Villager villager) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!CrownHelper.isCrowned(player)) {
                player.sendSystemMessage(Component.literal("§cVascani te ne jemljejo za kralja – nimas krone!"));
                return InteractionResult.CONSUME;
            }
            FeudalKnightEntity knight = com.suveren.feudal.entity.ModEntities.KNIGHT.get().create(level);
            if (knight == null) return InteractionResult.CONSUME;
            knight.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), 0);
            knight.setOwnerUUID(player.getUUID());
            knight.setCustomName(Component.literal(com.suveren.feudal.util.KnightNames.knight(level.random)));
            level.addFreshEntity(knight);
            villager.discard();
            ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    knight.getX(), knight.getY() + 1.5, knight.getZ(), 12, 0.4, 0.4, 0.4, 0.01);
            player.sendSystemMessage(Component.literal("§6" + knight.getName().getString()
                    + " ti je prisegel zvestobo – za vedno!"));
            return InteractionResult.CONSUME;
        }
        if (target instanceof FeudalKnightEntity knight) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!knight.obeys(player)) {
                player.sendSystemMessage(Component.literal("§cTa vitez te ne poslusa – potrebujes krono!"));
                return InteractionResult.CONSUME;
            }
            knight.cycleStance();
            player.sendSystemMessage(Component.literal("§e" + knight.getName().getString()
                    + " zdaj: §f" + FeudalKnightEntity.stanceLabel(knight.getStance())));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** Squat + desni klik na blok = dolocitev strazne tocke za najblizjega viteza. */
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) return InteractionResult.PASS;
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!CrownHelper.isCrowned(player)) {
            player.sendSystemMessage(Component.literal("§cBrez krone ne mores poveljevati!"));
            return InteractionResult.CONSUME;
        }
        BlockPos pos = ctx.getClickedPos();
        List<FeudalKnightEntity> knights = level.getEntitiesOfClass(
                FeudalKnightEntity.class, new AABB(pos).inflate(16),
                k -> k.isOwnedBy(player));
        if (knights.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7V blizini ni tvojih vitezov."));
            return InteractionResult.CONSUME;
        }
        knights.sort(Comparator.comparingDouble(k -> k.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())));
        FeudalKnightEntity k = knights.get(0);
        k.setGuardPos(pos);
        k.setStance(FeudalKnightEntity.Stance.GUARD);
        player.sendSystemMessage(Component.literal("§2Strazna tocka dolocena: §f" + k.getName().getString()
                + " §2(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
        return InteractionResult.CONSUME;
    }
}
