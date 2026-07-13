package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Compile-time stub for optional Sodium integration. This class is excluded from produced jars.
 */
public class BlockRenderer {
    protected LevelSlice slice;

    public void renderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin) {}

    protected void processQuad(MutableQuadViewImpl quad) {}
}