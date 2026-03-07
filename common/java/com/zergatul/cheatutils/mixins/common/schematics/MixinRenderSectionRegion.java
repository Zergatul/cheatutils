package com.zergatul.cheatutils.mixins.common.schematics;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import com.zergatul.cheatutils.schematics.WrapperRenderSectionRegion;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(RenderSectionRegion.class)
public abstract class MixinRenderSectionRegion implements RenderSectionRegionExtension, BlockAndTintGetter {

    @Shadow
    @Final
    private int minSectionX;

    @Shadow
    @Final
    private int minSectionY;

    @Shadow
    @Final
    private int minSectionZ;

    @Unique
    private SchematicaSectionCopy[] schematicaSections_CU;

    @Unique
    private boolean schematicaShadeBlocks_CU;

    @Unique
    private Map<ChunkSectionLayer, BufferBuilder> startedLayers_CU;

    @Unique
    private ModelBlockRenderer blockRenderer_CU;

    @Override
    public void setSchematicaSections_CU(SchematicaSectionCopy[] schematicaSections, boolean shade) {
        this.schematicaSections_CU = schematicaSections;
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
        return new WrapperRenderSectionRegion(this, schematicaSections_CU, minSectionX, minSectionY, minSectionZ);
    }

    @Override
    public void storeLocalVariables_CU(Map<ChunkSectionLayer, BufferBuilder> startedLayers, ModelBlockRenderer blockRenderer) {
        this.startedLayers_CU = startedLayers;
        this.blockRenderer_CU = blockRenderer;
    }

    @Override
    public Map<ChunkSectionLayer, BufferBuilder> getStartedLayers_CU() {
        return this.startedLayers_CU;
    }

    @Override
    public ModelBlockRenderer getBlockRenderer_CU() {
        return this.blockRenderer_CU;
    }
}