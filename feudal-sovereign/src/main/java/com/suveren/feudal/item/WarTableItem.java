
package com.suveren.feudal.item;

import com.suveren.feudal.FeudalMod;
import com.suveren.feudal.entity.FeudalKnightEntity;
import com.suveren.feudal.net.KnightInfo;
import com.suveren.feudal.net.OpenWarTablePacket;
import com.suveren.feudal.util.CrownHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Vojna karta: pregled vseh vitezov + ukazi cetam. */
public class WarTableItem extends Item {
    public WarTableItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            if (!CrownHelper.isCrowned(player)) {
                player.sendSystemMessage(Component.literal("§cVojaski svet te ne priznava brez krone!"));
                return InteractionResultHolder.consume(player.getItemInHand(hand));
            }
            List<KnightInfo> infos = new ArrayList<>();
            for (FeudalKnightEntity k : sl.getEntitiesOfClass(
                    FeudalKnightEntity.class, player.getBoundingBox().inflate(256), k -> k.isOwnedBy(player))) {
                infos.add(new KnightInfo(k.getUUID(), k.getName().getString(),
                        k.getStance().name(), Math.sqrt(k.distanceToSqr(player))));
            }
            FeudalMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new OpenWarTablePacket(infos, true));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
