
package com.suveren.feudal.net;

import com.suveren.feudal.FeudalMod;
import com.suveren.feudal.entity.FeudalKnightEntity;
import com.suveren.feudal.item.WarTableItem;
import com.suveren.feudal.util.CrownHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record SetStancePacket(UUID knightId, String stance) {

    public static void encode(SetStancePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.knightId());
        buf.writeUtf(msg.stance());
    }

    public static SetStancePacket decode(FriendlyByteBuf buf) {
        return new SetStancePacket(buf.readUUID(), buf.readUtf());
    }

    public static void handle(SetStancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!CrownHelper.isCrowned(player)) {
                player.sendSystemMessage(Component.literal("§cVojaki te ne poslusajo – nimas krone!"));
                return;
            }
            ServerLevel sl = player.serverLevel();
            for (FeudalKnightEntity k : sl.getEntitiesOfClass(
                    FeudalKnightEntity.class, player.getBoundingBox().inflate(256),
                    k -> k.getUUID().equals(msg.knightId()))) {
                if (!k.isOwnedBy(player)) continue;
                FeudalKnightEntity.Stance s = FeudalKnightEntity.Stance.valueOf(msg.stance());
                k.setStance(s);
                if (s == FeudalKnightEntity.Stance.GUARD && k.getGuardPos() == null) {
                    k.setGuardPos(k.blockPosition());
                }
                player.sendSystemMessage(Component.literal("§eUkaz: §f" + k.getName().getString()
                        + " §e-> §f" + FeudalKnightEntity.stanceLabel(s)));
            }
            // Poslji osvezen seznam za GUI.
            List<KnightInfo> infos = new ArrayList<>();
            for (FeudalKnightEntity k : sl.getEntitiesOfClass(
                    FeudalKnightEntity.class, player.getBoundingBox().inflate(256), k -> k.isOwnedBy(player))) {
                infos.add(new KnightInfo(k.getUUID(), k.getName().getString(),
                        k.getStance().name(), Math.sqrt(k.distanceToSqr(player))));
            }
            FeudalMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new OpenWarTablePacket(infos, false));
        });
        ctx.get().setPacketHandled(true);
    }
}
