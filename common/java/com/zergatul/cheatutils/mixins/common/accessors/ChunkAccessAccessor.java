package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ChunkAccess.class)
public interface ChunkAccessAccessor {

    @Accessor("heightmaps")
    Map<Heightmap.Types, Heightmap> getHeightmaps_CU();

    @Accessor("levelHeightAccessor")
    LevelHeightAccessor getLevelHeightAccessor_CU();
}