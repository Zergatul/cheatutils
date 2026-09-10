package com.zergatul.cheatutils.controllers;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class SnapshotChunk {

    private final Dimension dimension;
    private final ChunkPos pos;
    private final LevelHeightAccessor levelHeightAccessor;
    private final BitStorage heightmap;
    private final ExtendedBlockStorage[] sections;

    private SnapshotChunk(Dimension dimension, ChunkPos pos, LevelHeightAccessor levelHeightAccessor, BitStorage heightmap, ExtendedBlockStorage[] sections) {
        this.dimension = dimension;
        this.pos = pos;
        this.levelHeightAccessor = levelHeightAccessor;
        this.heightmap = heightmap;
        this.sections = sections;
    }

    public int getMinY() {
        return levelHeightAccessor.getMinY();
    }

    public ChunkPos getPos() {
        return pos;
    }

    public int getHeight(int x, int z) {
        return heightmap.get(x | (z << 4));
    }

    public IBlockAccess getBlockState(int x, int y, int z) {
        int index = (y - levelHeightAccessor.getMinY()) >> 4;
        return sections[index].getBlockState(x, y & 0x0F, z);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public static SnapshotChunk from(Chunk chunk) {
        // we just copy references for all objects since they are immutable
        // except LevelChunkSection.storageArrays
        // and heightMap
        ChunkAccessAccessor accessor = (ChunkAccessAccessor) chunk;
        LevelHeightAccessor levelHeightAccessor = accessor.getLevelHeightAccessor_CU();
        Heightmap surfaceHeightmap = accessor.getHeightmaps_CU().get(Heightmap.Types.WORLD_SURFACE);
        BitStorage heightmap = ((HeightmapAccessor) surfaceHeightmap).getData_CU().copy();
        LevelChunkSection[] sections = copySections(chunk.getSections());
        Dimension dimension = Dimension.get((ClientLevel) chunk.getLevel());
        return new SnapshotChunk(dimension, chunk.getPos(), levelHeightAccessor, heightmap, sections);
    }

    private static LevelChunkSection[] copySections(ExtendedBlockStorage[] source) {
        ExtendedBlockStorage[] destination = new ExtendedBlockStorage[source.length];
        for (int i = 0; i < source.length; i++) {
            ExtendedBlockStorage section = source[i];
            destination[i] = new LevelChunkSection(section.getStates().copy(), section.getBiomes());
        }
        return destination;
    }
}