package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;

public interface ChunkRenderContextExtension {
    SchematicaSectionCopy[] getSchematicaSections_CU();
    void setSchematicaSections_CU(SchematicaSectionCopy[] sections);
}