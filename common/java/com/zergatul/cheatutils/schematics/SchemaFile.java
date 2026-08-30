package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.state.BlockState;

public interface SchemaFile {

    long MAX_NBT_SIZE = (long) 1024 * 1024 * 1024; // 1Gb

    int getWidth();
    int getHeight();
    int getLength();
    BlockState getBlockState(int x, int y, int z);
    int[] getSummary();
    BlockState[] getPalette();
    String[] getRawPalette();
}