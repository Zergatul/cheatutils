package com.zergatul.cheatutils.schematics;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.OutputStream;

public interface SchemaFile {
    int getWidth();
    int getHeight();
    int getLength();
    BlockState getBlockState(int x, int y, int z);
    int[] getSummary();
    BlockState[] getPalette();
    void write(OutputStream output) throws IOException;

    default Vec3i getSize() {
        return new Vec3i(getWidth(), getHeight(), getLength());
    }

    default void copyTo(SchemaFileEditable file, int destX, int destY, int destZ) throws MissingPaletteEntryException {
        int width = getWidth();
        int height = getHeight();
        int length = getLength();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    BlockState state = getBlockState(x, y, z);
                    file.setBlockState(destX + x, destY + y, destZ + z, state);
                }
            }
        }
    }
}