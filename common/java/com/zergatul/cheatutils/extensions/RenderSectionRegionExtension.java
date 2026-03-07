package com.zergatul.cheatutils.extensions;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.zergatul.cheatutils.schematics.SchematicaSectionCopy;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import java.util.Map;

public interface RenderSectionRegionExtension {
    void setSchematicaSections_CU(SchematicaSectionCopy[] copies, boolean shade);
    boolean hasSchematicaBlocks_CU();
    boolean shadeSchematicaBlocks_CU();
    BlockAndTintGetter asWrapped_CU();
    void storeLocalVariables_CU(Map<ChunkSectionLayer, BufferBuilder> startedLayers, ModelBlockRenderer blockRenderer, FluidRenderer fluidRenderer);
    Map<ChunkSectionLayer, BufferBuilder> getStartedLayers_CU();
    ModelBlockRenderer getBlockRenderer_CU();
    FluidRenderer getFluidRenderer_CU();
}