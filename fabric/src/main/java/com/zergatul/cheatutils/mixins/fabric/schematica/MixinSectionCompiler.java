package com.zergatul.cheatutils.mixins.fabric.schematica;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
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
    @Final
    private LiquidBlockRenderer liquidRenderer;

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> map, SectionBufferBuilderPack pack, ChunkSectionLayer layer);

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"))
    private void onBeforeTesselating(
            SectionPos sectionPos,
            RenderSectionRegion region,
            VertexSorting vertexSorting,
            SectionBufferBuilderPack builders,
            CallbackInfoReturnable<SectionCompiler.Results> info,
            @Local(name = "startedLayers") Map<ChunkSectionLayer, BufferBuilder> startedLayers,
            @Local(name = "blockRenderer") ModelBlockRenderer blockRenderer
    ) {
        RenderSectionRegionExtension extension = (RenderSectionRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        extension.storeLocalVariables_CU(startedLayers, blockRenderer);
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

        Map<ChunkSectionLayer, BufferBuilder> startedLayers = extension.getStartedLayers_CU();
        ModelBlockRenderer blockRenderer = extension.getBlockRenderer_CU();

        boolean shaded = extension.shadeSchematicaBlocks_CU();
        BlockAndTintGetter wrapped = extension.asWrapped_CU();

        BlockQuadOutput quadOutput = (x, y, z, quad, instance) -> {
            VertexConsumer consumer = this.getOrBeginLayer_CU(startedLayers, builders, quad.spriteInfo().layer(), shaded);
            consumer.putBlockBakedQuad(x, y, z, quad, instance);
        };
        BlockQuadOutput opaqueQuadOutput = (x, y, z, quad, instance) -> {
            VertexConsumer consumer = this.getOrBeginLayer_CU(startedLayers, builders, ChunkSectionLayer.SOLID, shaded);
            consumer.putBlockBakedQuad(x, y, z, quad, instance);
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
                ChunkSectionLayer layer = this.liquidRenderer.getRenderLayer(fluidState);
                this.liquidRenderer.tesselate(region, pos, getOrBeginLayer_CU(startedLayers, builders, layer, shaded), state, fluidState);
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
    private VertexConsumer getOrBeginLayer_CU(Map<ChunkSectionLayer, BufferBuilder> map, SectionBufferBuilderPack pack, ChunkSectionLayer layer, boolean shaded) {
        VertexConsumer consumer = this.getOrBeginLayer(map, pack, layer);
        if (shaded) {
            return new ShadedVertexConsumerWrapper(consumer);
        } else {
            return consumer;
        }
    }
}