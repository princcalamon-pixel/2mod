
package com.suveren.feudal.item;

import com.suveren.feudal.entity.FeudalFarmerEntity;
import com.suveren.feudal.util.CrownHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FarmerContractItem extends Item {
    public FarmerContractItem(Properties props) { super(props); }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity target, net.minecraft.world.InteractionHand hand) {
        if (!(target instanceof Villager villager)) return InteractionResult.PASS;
        Level level = player.level();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!CrownHelper.isCrowned(player)) {
            player.sendSystemMessage(Component.literal("§cVascani te ne jemljejo za kralja – nimas krone!"));
            return InteractionResult.CONSUME;
        }
        FeudalFarmerEntity farmer = com.suveren.feudal.entity.ModEntities.FARMER.get().create(level);
        if (farmer == null) return InteractionResult.CONSUME;
        farmer.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), 0);
        farmer.setOwnerUUID(player.getUUID());
        farmer.setHome(villager.blockPosition());
        farmer.setCustomName(Component.literal(com.suveren.feudal.util.KnightNames.farmer(level.random)));
        level.addFreshEntity(farmer);
        villager.discard();
        ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                farmer.getX(), farmer.getY() + 1.5, farmer.getZ(), 12, 0.4, 0.4, 0.4, 0.01);
        player.sendSystemMessage(Component.literal("§a" + farmer.getName().getString()
                + " je podpisal pogodbo: ore, saje, pobira davek v zitnico."));
        stack.shrink(1);
        return InteractionResult.CONSUME;
    }
}
