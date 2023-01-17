package com.zergatul.cheatutils.schematics;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.OutputStream;

public interface SchemaFile {
    int getWidth();
    int getHeight();
    int getLength();
    Block getBlock(int x, int y, int z);
    int[] getSummary();
    Block[] getPalette();
    void write(OutputStream output) throws IOException;

    default Vec3i getSize() {
        return new Vec3i(getWidth(), getHeight(), getLength());
    }
}