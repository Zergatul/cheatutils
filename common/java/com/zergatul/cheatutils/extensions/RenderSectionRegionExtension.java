package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.minecraft.core.BlockPos;

public interface RenderSectionRegionExtension {
    void setSchematicaSections_CU(SchematicaSectionCopy[] copies, boolean shade);
    boolean hasSchematicaBlockAt_CU(BlockPos pos);
}