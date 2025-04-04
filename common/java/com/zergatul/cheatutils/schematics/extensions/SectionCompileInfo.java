package com.zergatul.cheatutils.schematics.extensions;

import com.zergatul.cheatutils.modules.automation.Schematica;
import net.minecraft.core.SectionPos;

public class SectionCompileInfo {

    public SectionPos sectionPos;
    public boolean shouldRenderSchematicaGhostBlocks;
    public Schematica.SectionInfo schematicaSectionInfo;

    public void clear() {
        sectionPos = null;
        shouldRenderSchematicaGhostBlocks = false;
        schematicaSectionInfo = null;
    }
}