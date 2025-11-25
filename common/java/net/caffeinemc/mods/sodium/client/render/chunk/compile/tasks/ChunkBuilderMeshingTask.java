package net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
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

        for (int y = minY; y < maxY; y++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int x = minX; x < maxX; x++) {
                    BlockState blockState = slice.getBlockState(x, y, z);
                    throw new AssertionError();
                }
            }
        }

        throw new AssertionError();
    }
}