package com.zergatul.cheatutils.common.events;

import com.zergatul.cheatutils.mixins.accessors.ChunkAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class SnapshotChunk {

    //private final Dimension dimension;
    private final ChunkPos pos;
    private final int[] heightMap;
    private final ExtendedBlockStorage[] sections;

    private SnapshotChunk(/*Dimension dimension, */ChunkPos pos, int[] heightMap, ExtendedBlockStorage[] sections) {
        //this.dimension = dimension;
        this.pos = pos;
        this.heightMap = heightMap;
        this.sections = sections;
    }

    public ChunkPos getPos() {
        return pos;
    }

    public int getHeight(int x, int z) {
        return heightMap[(z << 4) | x];
    }

    public IBlockState getBlockState(int x, int y, int z) {
        return sections[y >> 4].get(x, y & 0x0F, z);
    }

    /*public Dimension getDimension() {
        return dimension;
    }*/

    public static SnapshotChunk from(Chunk chunk) {
        // we just copy references for all objects since they are immutable
        // except LevelChunkSection.storageArrays
        // and heightMap
        ChunkAccessor accessor = (ChunkAccessor) chunk;
        ExtendedBlockStorage[] sections = copySections(chunk.getBlockStorageArray());
        //Dimension dimension = Dimension.get((ClientLevel) chunk.getLevel());
        return new SnapshotChunk(/*dimension,*/ chunk.getPos(), accessor.getHeightMap_CU() /*temp no copy*/, sections);
    }

    private static ExtendedBlockStorage[] copySections(ExtendedBlockStorage[] source) {
        ExtendedBlockStorage[] destination = new ExtendedBlockStorage[source.length];
        for (int i = 0; i < source.length; i++) {
            ExtendedBlockStorage section = source[i];
            // TEMP: no copy, just pointer
            destination[i] = section; //new ExtendedBlockStorage(section.getStates().copy(), section.getBiomes());
        }
        return destination;
    }
}