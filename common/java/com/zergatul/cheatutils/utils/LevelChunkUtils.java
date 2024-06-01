package com.zergatul.cheatutils.utils;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class LevelChunkUtils {

    public static BlockState getBlockState(LevelChunk chunk, int x, int y, int z) {
        int index = chunk.getSectionIndex(y);
        LevelChunkSection[] sections = chunk.getSections();
        if (index >= 0 && index < sections.length) {
            LevelChunkSection levelchunksection = sections[index];
            if (!levelchunksection.hasOnlyAir()) {
                return levelchunksection.getBlockState(x & 15, y & 15, z & 15);
            }
        }

        return Blocks.AIR.defaultBlockState();
    }
}