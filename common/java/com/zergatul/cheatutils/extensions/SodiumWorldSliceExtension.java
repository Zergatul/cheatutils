package com.zergatul.cheatutils.extensions;

import net.minecraft.world.level.block.state.BlockState;

public interface SodiumWorldSliceExtension {
    boolean hasSchematicaBlocks_CU();
    BlockState getSchematicaBlockState_CU(int x, int y, int z);
    void setSchematicaView_CU(boolean value);
    boolean shadeSchematicaBlocks_CU();
}