package com.zergatul.cheatutils.schematics;

import net.minecraft.world.level.block.Block;

public interface SchemaFileEditable extends SchemaFile {
    void setBlock(int x, int y, int z, Block block) throws MissingPaletteEntryException;
    void setPaletteEntry(int index, Block block);
}