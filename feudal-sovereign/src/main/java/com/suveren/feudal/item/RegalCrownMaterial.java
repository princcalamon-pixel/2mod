
package com.suveren.feudal.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public enum RegalCrownMaterial implements ArmorMaterial {
    INSTANCE;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) { return 222; }

    @Override
    public int getDefenseForType(ArmorItem.Type type) { return type == ArmorItem.Type.HELMET ? 2 : 0; }

    @Override
    public int getEnchantmentValue() { return 18; }

    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_GOLD; }

    @Override
    public Ingredient getRepairIngredient() { return Ingredient.of(new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)); }

    @Override
    public String getName() { return "suveren_crown"; }

    @Override
    public float getToughness() { return 0.5f; }

    @Override
    public float getKnockbackResistance() { return 0.0f; }
}
