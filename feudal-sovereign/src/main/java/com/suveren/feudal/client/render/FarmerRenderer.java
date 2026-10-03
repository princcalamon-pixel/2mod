
package com.suveren.feudal.client.render;

import com.suveren.feudal.entity.FeudalFarmerEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FarmerRenderer extends MobRenderer<FeudalFarmerEntity, VillagerModel<FeudalFarmerEntity>> {
    public FarmerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(FeudalFarmerEntity entity) {
        return new ResourceLocation("minecraft", "textures/entity/villager/villager.png");
    }
}
