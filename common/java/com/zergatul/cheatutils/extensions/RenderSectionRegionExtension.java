package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.minecraft.world.level.BlockAndTintGetter;

public interface RenderSectionRegionExtension {
    void setSchematicaSections_CU(SchematicaSectionCopy[] copies, boolean shade);
    boolean hasSchematicaBlocks_CU();
    boolean shadeSchematicaBlocks_CU();
    BlockAndTintGetter asWrapped_CU();
}