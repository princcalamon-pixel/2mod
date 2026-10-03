
package com.suveren.feudal.item;

import com.suveren.feudal.FeudalMod;
import com.suveren.feudal.block.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FeudalMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, FeudalMod.MODID);

    public static final RegistryObject<Item> REGAL_CROWN = ITEMS.register("regal_crown",
            () -> new ArmorItem(RegalCrownMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ROYAL_SCEPTER = ITEMS.register("royal_scepter",
            () -> new RoyalScepterItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WAR_TABLE = ITEMS.register("war_table",
            () -> new WarTableItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GRANARY_DEED = ITEMS.register("granary_deed",
            () -> new DeedItem(new Item.Properties(), false));

    public static final RegistryObject<Item> ARMORY_DEED = ITEMS.register("armory_deed",
            () -> new DeedItem(new Item.Properties(), true));

    public static final RegistryObject<Item> FARMER_CONTRACT = ITEMS.register("farmer_contract",
            () -> new FarmerContractItem(new Item.Properties()));

    public static final RegistryObject<Item> HERALDIC_BANNER_ITEM = ITEMS.register("heraldic_banner",
            () -> new BlockItem(ModBlocks.HERALDIC_BANNER.get(), new Item.Properties()));

    public static final RegistryObject<Item> KNIGHT_EGG = ITEMS.register("knight_spawn_egg",
            () -> new ForgeSpawnEggItem(com.suveren.feudal.entity.ModEntities.KNIGHT, 0xB0B0C0, 0x8B0000, new Item.Properties()));
    public static final RegistryObject<Item> BANDIT_EGG = ITEMS.register("bandit_spawn_egg",
            () -> new ForgeSpawnEggItem(com.suveren.feudal.entity.ModEntities.BANDIT, 0x3A3A3A, 0x700000, new Item.Properties()));
    public static final RegistryObject<Item> FARMER_EGG = ITEMS.register("farmer_spawn_egg",
            () -> new ForgeSpawnEggItem(com.suveren.feudal.entity.ModEntities.FARMER, 0x8B5A2B, 0x3CB371, new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("suveren",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.suveren"))
                    .icon(() -> new ItemStack(REGAL_CROWN.get()))
                    .displayItems((params, out) -> {
                        out.accept(REGAL_CROWN.get());
                        out.accept(ROYAL_SCEPTER.get());
                        out.accept(WAR_TABLE.get());
                        out.accept(HERALDIC_BANNER_ITEM.get());
                        out.accept(GRANARY_DEED.get());
                        out.accept(ARMORY_DEED.get());
                        out.accept(FARMER_CONTRACT.get());
                        out.accept(KNIGHT_EGG.get());
                        out.accept(BANDIT_EGG.get());
                        out.accept(FARMER_EGG.get());
                    })
                    .build());
}
