package com.zergatul.cheatutils.tests.utility;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@NullMarked
public class MockChunkSource extends ChunkSource {

    private final MockLevel level;
    private final List<MockChunk> chunks;

    public MockChunkSource(MockLevel level) {
        this.level = level;
        this.chunks = new ArrayList<>();
    }

    @Override
    public @Nullable ChunkAccess getChunk(int x, int z, ChunkStatus targetStatus, boolean loadOrGenerate) {
        return chunks.stream()
                .filter(c -> c.getPos().x() == x && c.getPos().z() == z)
                .findFirst()
                .orElseGet(() -> {
                    MockChunk chunk = new MockChunk(level, new ChunkPos(x, z));
                    chunks.add(chunk);
                    return chunk;
                });
    }

    @Override
    public void tick(BooleanSupplier haveTime, boolean tickChunks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String gatherStats() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLoadedChunksCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public BlockGetter getLevel() {
        return level;
    }
}