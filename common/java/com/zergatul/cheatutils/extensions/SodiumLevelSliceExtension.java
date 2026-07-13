package com.zergatul.cheatutils.extensions;

import net.minecraft.world.level.block.state.BlockState;

public interface SodiumLevelSliceExtension {
    boolean hasSchematicaBlocks_CU();
    BlockState getSchematicaBlockState_CU(int x, int y, int z);
    boolean isSchematicaView_CU();
    void setSchematicaView_CU(boolean value);
    boolean shadeSchematicaBlocks_CU();
}