
package com.suveren.feudal.net;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record OpenWarTablePacket(List<KnightInfo> knights, boolean openScreen) {

    public static void encode(OpenWarTablePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.openScreen);
        buf.writeInt(msg.knights.size());
        for (KnightInfo k : msg.knights) {
            buf.writeUUID(k.id());
            buf.writeUtf(k.name());
            buf.writeUtf(k.stance());
            buf.writeDouble(k.distance());
        }
    }

    public static OpenWarTablePacket decode(FriendlyByteBuf buf) {
        boolean open = buf.readBoolean();
        int n = buf.readInt();
        List<KnightInfo> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new KnightInfo(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readDouble()));
        }
        return new OpenWarTablePacket(list, open);
    }

    public static void handle(OpenWarTablePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.suveren.feudal.client.WarTableScreen.CACHED = msg.knights();
            if (msg.openScreen() && !(Minecraft.getInstance().screen instanceof com.suveren.feudal.client.WarTableScreen)) {
                Minecraft.getInstance().setScreen(new com.suveren.feudal.client.WarTableScreen());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
