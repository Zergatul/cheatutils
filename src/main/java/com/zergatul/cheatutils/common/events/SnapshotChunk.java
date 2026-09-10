package com.zergatul.cheatutils.common.events;

import com.zergatul.cheatutils.mixins.accessors.ChunkAccessor;
import com.zergatul.cheatutils.modules.esp.blocks.SectionBlockStates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class SnapshotChunk {

    //private final Dimension dimension;
    private final ChunkPos pos;
    private final SectionBlockStates[] sections;

    private SnapshotChunk(/*Dimension dimension, */ChunkPos pos, SectionBlockStates[] sections) {
        //this.dimension = dimension;
        this.pos = pos;
        this.sections = sections;
    }

    public ChunkPos getPos() {
        return pos;
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
        ChunkAccessor accessor = (ChunkAccessor) chunk;
        SectionBlockStates[] sections = copySections(chunk.getBlockStorageArray());
        //Dimension dimension = Dimension.get((ClientLevel) chunk.getLevel());
        return new SnapshotChunk(/*dimension,*/ chunk.getPos(), sections);
    }

    private static SectionBlockStates[] copySections(ExtendedBlockStorage[] source) {
        SectionBlockStates[] destination = new SectionBlockStates[source.length];
        for (int i = 0; i < source.length; i++) {
            ExtendedBlockStorage section = source[i];
            if (section != null) {
                destination[i] = SectionBlockStates.from(source[i].getData());
            } else {
                destination[i] = SectionBlockStates.EMPTY;
            }
        }
        return destination;
    }
}