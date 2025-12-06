package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.ChunkRenderContextExtension;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ChunkRenderContext.class, remap = false)
public abstract class MixinChunkRenderContext implements ChunkRenderContextExtension {

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    public SchematicaSectionCopy[] getSchematicaSections_CU() {
        return schematicaSections_CU;
    }

    public void setSchematicaSections_CU(SchematicaSectionCopy[] sections) {
        schematicaSections_CU = sections;
    }
}