package com.zergatul.cheatutils.mixins.accessors;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Chunk.class)
public interface ChunkAccessor {

    @Accessor("heightMap")
    int[] getHeightMap_CU();
}