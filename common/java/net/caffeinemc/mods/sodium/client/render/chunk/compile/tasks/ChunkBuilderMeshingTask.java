package net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.world.level.block.state.BlockState;

public class ChunkBuilderMeshingTask extends ChunkBuilderTask<ChunkBuildOutput> {

    @Override
    public ChunkBuildOutput execute(ChunkBuildContext buildContext, CancellationToken cancellationToken) {
        LevelSlice slice = new LevelSlice();

        int minX = 0;
        int minY = 0;
        int minZ = 0;

        int maxX = minX + 16;
        int maxY = minY + 16;
        int maxZ = minZ + 16;

        BlockRenderer blockRenderer = buildContext.cache.getBlockRenderer();

        for (int y = minY; y < maxY; y++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int x = minX; x < maxX; x++) {
                    BlockState blockState = slice.getBlockState(x, y, z);
                    blockRenderer.renderModel(null, blockState, null, null);
                    throw new AssertionError();
                }
            }
        }

        throw new AssertionError();
    }
}