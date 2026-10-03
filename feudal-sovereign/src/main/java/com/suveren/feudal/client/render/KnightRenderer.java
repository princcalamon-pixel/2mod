
package com.suveren.feudal.client.render;

import com.suveren.feudal.entity.FeudalKnightEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class KnightRenderer extends MobRenderer<FeudalKnightEntity, HumanoidModel<FeudalKnightEntity>> {
    public KnightRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(FeudalKnightEntity entity) {
        return new ResourceLocation("minecraft", "textures/entity/illager/evoker.png");
    }
}
