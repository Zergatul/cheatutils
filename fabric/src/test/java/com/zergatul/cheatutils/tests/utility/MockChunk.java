package com.zergatul.cheatutils.tests.utility;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MockChunk extends LevelChunk {
    public MockChunk(MockLevel level, ChunkPos chunkPos) {
        super(level, chunkPos);
    }
}