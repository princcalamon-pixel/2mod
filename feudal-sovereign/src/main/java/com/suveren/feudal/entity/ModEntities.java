
package com.suveren.feudal.entity;

import com.suveren.feudal.FeudalMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FeudalMod.MODID);

    public static final RegistryObject<EntityType<FeudalKnightEntity>> KNIGHT = ENTITIES.register("knight",
            () -> EntityType.Builder.of(FeudalKnightEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("suveren:knight"));

    public static final RegistryObject<EntityType<BanditEntity>> BANDIT = ENTITIES.register("bandit",
            () -> EntityType.Builder.of(BanditEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("suveren:bandit"));

    public static final RegistryObject<EntityType<FeudalFarmerEntity>> FARMER = ENTITIES.register("farmer",
            () -> EntityType.Builder.of(FeudalFarmerEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("suveren:farmer"));

    @Mod.EventBusSubscriber(modid = FeudalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    static class Attributes {
        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent e) {
            e.put(KNIGHT.get(), FeudalKnightEntity.createAttributes().build());
            e.put(BANDIT.get(), BanditEntity.createAttributes().build());
            e.put(FARMER.get(), FeudalFarmerEntity.createAttributes().build());
        }
    }
}
