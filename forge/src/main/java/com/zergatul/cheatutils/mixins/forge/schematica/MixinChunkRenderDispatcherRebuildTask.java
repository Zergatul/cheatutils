package com.zergatul.cheatutils.mixins.forge.schematica;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.extensions.RenderChunkRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import com.zergatul.mixin.LiteInject;
import com.zergatul.mixin.LocalVariable;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class MixinChunkRenderDispatcherRebuildTask {

    @LiteInject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private void onBeforeTransparencySorting(
            @LocalVariable(ordinal = 0) ChunkBufferBuilderPack pack,
            @LocalVariable(ordinal = 0) BlockPos origin,
            @LocalVariable(ordinal = 0) RenderChunkRegion region,
            @LocalVariable(ordinal = 0) PoseStack poseStack,
            @LocalVariable(ordinal = 0) Set<RenderType> layers,
            @LocalVariable(ordinal = 0) RandomSource random,
            @LocalVariable(ordinal = 0) BlockRenderDispatcher blockRenderer
    ) {
        RenderChunkRegionExtension extension = (RenderChunkRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        boolean shaded = extension.shadeSchematicaBlocks_CU();
        BlockAndTintGetter wrapped = extension.asWrapped_CU();
        BlockPos corner2 = origin.offset(15, 15, 15);
        for (BlockPos pos : BlockPos.betweenClosed(origin, corner2)) {
            if (!region.getBlockState(pos).isAir()) {
                continue;
            }

            BlockState state = wrapped.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
                blockRenderer.renderLiquid(pos, wrapped, getOrBeginLayer_CU(pack, layers, layer, shaded), state, fluidState);
            }

            if (state.getRenderShape() != RenderShape.INVISIBLE) {
                BakedModel model = blockRenderer.getBlockModel(state);
                ModelData modelData = model.getModelData(wrapped, pos, state, ModelData.EMPTY);
                random.setSeed(state.getSeed(pos));
                for (RenderType layer : model.getRenderTypes(state, random, modelData)) {
                    poseStack.pushPose();
                    poseStack.translate(
                            SectionPos.sectionRelative(pos.getX()),
                            SectionPos.sectionRelative(pos.getY()),
                            SectionPos.sectionRelative(pos.getZ()));
                    blockRenderer.renderBatched(
                            state,
                            pos,
                            wrapped,
                            poseStack,
                            getOrBeginLayer_CU(pack, layers, layer, shaded),
                            true,
                            random,
                            modelData,
                            layer);
                    poseStack.popPose();
                }
            }
        }
    }

    @Unique
    private VertexConsumer getOrBeginLayer_CU(
            ChunkBufferBuilderPack pack,
            Set<RenderType> layers,
            RenderType layer,
            boolean shaded
    ) {
        BufferBuilder builder = pack.builder(layer);
        if (layers.add(layer)) {
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }
        return shaded ? new ShadedVertexConsumerWrapper(builder) : builder;
    }
}