package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.minecraft.world.level.BlockAndTintGetter;

public interface RenderChunkRegionExtension {
    void setSchematicaSections_CU(
            SchematicaSectionCopy[] copies,
            int minSectionX,
            int minSectionY,
            int minSectionZ,
            boolean shade);
    boolean hasSchematicaBlocks_CU();
    boolean shadeSchematicaBlocks_CU();
    BlockAndTintGetter asWrapped_CU();
}