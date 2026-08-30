package com.zergatul.cheatutils.mixins.common.schematica;

import com.zergatul.cheatutils.extensions.RenderChunkRegionExtension;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import com.zergatul.cheatutils.schematics.WrapperRenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderChunkRegion.class)
public abstract class MixinRenderChunkRegion implements RenderChunkRegionExtension, BlockAndTintGetter {

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    @Unique
    private int schematicaMinSectionX_CU;

    @Unique
    private int schematicaMinSectionY_CU;

    @Unique
    private int schematicaMinSectionZ_CU;

    @Unique
    private boolean schematicaShadeBlocks_CU;

    @Override
    public void setSchematicaSections_CU(
            SchematicaSectionCopy[] copies,
            int minSectionX,
            int minSectionY,
            int minSectionZ,
            boolean shade
    ) {
        this.schematicaSections_CU = copies;
        this.schematicaMinSectionX_CU = minSectionX;
        this.schematicaMinSectionY_CU = minSectionY;
        this.schematicaMinSectionZ_CU = minSectionZ;
        this.schematicaShadeBlocks_CU = shade;
    }

    @Override
    public boolean hasSchematicaBlocks_CU() {
        return schematicaSections_CU != null;
    }

    @Override
    public boolean shadeSchematicaBlocks_CU() {
        return schematicaShadeBlocks_CU;
    }

    @Override
    public BlockAndTintGetter asWrapped_CU() {
        return new WrapperRenderChunkRegion(
                this,
                schematicaSections_CU,
                schematicaMinSectionX_CU,
                schematicaMinSectionY_CU,
                schematicaMinSectionZ_CU);
    }
}