
package com.suveren.feudal.client.render;

import com.suveren.feudal.entity.BanditEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BanditRenderer extends MobRenderer<BanditEntity, HumanoidModel<BanditEntity>> {
    public BanditRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(BanditEntity entity) {
        return new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");
    }
}
