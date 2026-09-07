package com.zergatul.cheatutils.mixins.accessors;

import net.minecraft.util.BitArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BitArray.class)
public interface BitArrayAccessor {

    @Accessor("bitsPerEntry")
    int getBitsPerEntry_CU();
}