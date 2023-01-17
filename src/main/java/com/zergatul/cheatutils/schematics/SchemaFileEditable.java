package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.state.BlockState;

public interface SchemaFileEditable extends SchemaFile {
    void setBlockState(int x, int y, int z, BlockState state) throws MissingPaletteEntryException;
    void setPaletteEntry(int index, BlockState state);
}