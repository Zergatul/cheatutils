package com.zergatul.cheatutils.schematics;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;

public interface SchemaFile {
    int getWidth();
    int getHeight();
    int getLength();
    Block getBlock(int x, int y, int z);
    int[] getSummary();
    Block[] getPalette();

    default Vec3i getSize() {
        return new Vec3i(getWidth(), getHeight(), getLength());
    }
}