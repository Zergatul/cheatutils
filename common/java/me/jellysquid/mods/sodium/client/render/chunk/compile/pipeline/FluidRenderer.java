package me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;

public abstract class FluidRenderer {
    public abstract void render(
            WorldSlice slice,
            FluidState fluidState,
            BlockPos blockPos,
            BlockPos modelOffset,
            ChunkBuildBuffers buffers);
}