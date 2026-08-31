package me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRenderContext {
    public BlockRenderContext(WorldSlice world) {}

    public void update(BlockPos pos, BlockPos origin, BlockState state, BakedModel model, long seed) {}
}