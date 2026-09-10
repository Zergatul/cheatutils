package com.zergatul.cheatutils.mixins.accessors;

import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStateContainer.class)
public interface BlockStateContainerAccessor {

    @Accessor("palette")
    IBlockStatePalette getPalette_CU();

    @Accessor("storage")
    BitArray getStorage_CU();
}