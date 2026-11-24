package com.zergatul.cheatutils.mixins.fabric.schematica;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.SectionBufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Shadow
    @Final
    private boolean cutoutLeaves;

    @Shadow
    @Final
    private BlockStateModelSet blockModelSet;

    @Shadow
    protected abstract SectionBufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, SectionBufferBuilder> startedLayers, SectionBufferBuilderPack buffers, ChunkSectionLayer layer);

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"))
    private void onBeforeTesselating(
            SectionPos sectionPos,
            RenderSectionRegion region,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack builders,
            CallbackInfoReturnable<SectionCompiler.Results> info,
            @Local(name = "startedLayers") Map<ChunkSectionLayer, SectionBufferBuilder> startedLayers,
            @Local(name = "blockRenderer") ModelBlockRenderer blockRenderer,
            @Local(name = "fluidRenderer") FluidRenderer fluidRenderer
    ) {
        RenderSectionRegionExtension extension = (RenderSectionRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        extension.storeLocalVariables_CU(startedLayers, blockRenderer, fluidRenderer);
    }

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private void onBeforeSorting(
            SectionPos sectionPos,
            RenderSectionRegion region,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack builders,
            CallbackInfoReturnable<SectionCompiler.Results> info
    ) {
        RenderSectionRegionExtension extension = (RenderSectionRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        Map<ChunkSectionLayer, SectionBufferBuilder> startedLayers = extension.getStartedLayers_CU();
        ModelBlockRenderer blockRenderer = extension.getBlockRenderer_CU();
        FluidRenderer fluidRenderer = extension.getFluidRenderer_CU();

        boolean shaded = extension.shadeSchematicaBlocks_CU();
        BlockAndTintGetter wrapped = extension.asWrapped_CU();

        BlockQuadOutput quadOutput = (x, y, z, quad, instance) -> {
            SectionBufferBuilder builder = this.getOrBeginLayer(startedLayers, builders, quad.materialInfo().layer());
            addQuad_CU(builder, x, y, z, quad, instance, shaded);
        };
        BlockQuadOutput opaqueQuadOutput = (x, y, z, quad, instance) -> {
            SectionBufferBuilder builder = this.getOrBeginLayer(startedLayers, builders, ChunkSectionLayer.SOLID);
            addQuad_CU(builder, x, y, z, quad, instance, shaded);
        };
        FluidRenderer.Output fluidOutput = (layer, x, y, z, quad, color, brightness, lightCoords) -> {
            SectionBufferBuilder builder = this.getOrBeginLayer(startedLayers, builders, layer);
            builder.addFluidQuad(x, y, z, quad, shaded ? shadeColor_CU(color) : color, brightness, lightCoords);
        };

        BlockPos corner1 = sectionPos.origin();
        BlockPos corner2 = corner1.offset(15, 15, 15);
        for (BlockPos pos : BlockPos.betweenClosed(corner1, corner2)) {
            if (!region.getBlockState(pos).isAir()) {
                continue;
            }

            BlockState state = wrapped.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                fluidRenderer.tesselate(wrapped, pos, fluidOutput, state, fluidState);
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                blockRenderer.tesselateBlock(
                        ModelBlockRenderer.forceOpaque(this.cutoutLeaves, state) ? opaqueQuadOutput : quadOutput,
                        SectionPos.sectionRelative(pos.getX()),
                        SectionPos.sectionRelative(pos.getY()),
                        SectionPos.sectionRelative(pos.getZ()),
                        wrapped,
                        pos,
                        state,
                        this.blockModelSet.get(state),
                        state.getSeed(pos));
            }
        }
    }

    @Unique
    private static void addQuad_CU(SectionBufferBuilder builder, float x, float y, float z, BakedQuad quad, QuadInstance instance, boolean shaded) {
        if (!shaded) {
            builder.addQuad(x, y, z, quad, instance);
            return;
        }

        int color = instance.getColor();
        instance.setColor(shadeColor_CU(color));
        try {
            builder.addQuad(x, y, z, quad, instance);
        } finally {
            instance.setColor(color);
        }
    }

    @Unique
    private static int shadeColor_CU(int color) {
        return ARGB.color(
                Math.round(0.6f * ARGB.alpha(color)),
                Math.round(0.5f * ARGB.red(color)),
                Math.round(0.8f * ARGB.green(color)),
                ARGB.blue(color));
    }
}