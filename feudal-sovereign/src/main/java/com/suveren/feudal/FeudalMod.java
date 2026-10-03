
package com.suveren.feudal;

import com.suveren.feudal.block.ModBlocks;
import com.suveren.feudal.entity.ModEntities;
import com.suveren.feudal.item.ModItems;
import com.suveren.feudal.net.OpenWarTablePacket;
import com.suveren.feudal.net.SetStancePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FeudalMod.MODID)
public class FeudalMod {
    public static final String MODID = "suveren";
    public static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    public FeudalMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModEntities.ENTITIES.register(bus);

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
        int i = 0;
        CHANNEL.registerMessage(i++, OpenWarTablePacket.class,
                OpenWarTablePacket::encode, OpenWarTablePacket::decode, OpenWarTablePacket::handle);
        CHANNEL.registerMessage(i++, SetStancePacket.class,
                SetStancePacket::encode, SetStancePacket::decode, SetStancePacket::handle);

        LOGGER.info("Feudal Sovereign: dol zivitev, kralj!");
    }
}
