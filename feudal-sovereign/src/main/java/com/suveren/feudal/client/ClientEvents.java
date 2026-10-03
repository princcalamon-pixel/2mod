
package com.suveren.feudal.client;

import com.suveren.feudal.FeudalMod;
import com.suveren.feudal.client.render.BanditRenderer;
import com.suveren.feudal.client.render.FarmerRenderer;
import com.suveren.feudal.client.render.KnightRenderer;
import com.suveren.feudal.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeudalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.KNIGHT.get(), KnightRenderer::new);
        e.registerEntityRenderer(ModEntities.BANDIT.get(), BanditRenderer::new);
        e.registerEntityRenderer(ModEntities.FARMER.get(), FarmerRenderer::new);
    }
}
