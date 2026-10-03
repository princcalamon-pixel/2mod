
package com.suveren.feudal.util;

import com.suveren.feudal.item.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class CrownHelper {
    // Brez krone te vojaki jemljejo za navadnega kmeta.
    public static boolean isCrowned(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.REGAL_CROWN.get());
    }
}
