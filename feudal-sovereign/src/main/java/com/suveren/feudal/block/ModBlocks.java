
package com.suveren.feudal.block;

import com.suveren.feudal.FeudalMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FeudalMod.MODID);

    public static final RegistryObject<Block> HERALDIC_BANNER = BLOCKS.register("heraldic_banner",
            () -> new HeraldicBannerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED).strength(1.2f).sound(SoundType.WOOD).noOcclusion()));
}
