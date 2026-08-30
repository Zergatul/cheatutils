package com.zergatul.cheatutils.mixins.neoforge.schematica;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.extensions.RenderChunkRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import com.zergatul.mixin.LiteInject;
import com.zergatul.mixin.LocalVariable;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack pack, RenderType layer);

    @LiteInject(
            method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;addAdditionalGeometry(Ljava/util/List;Ljava/util/function/Function;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void onBeforeSorting(
            @LocalVariable(ordinal = 0) SectionPos sectionPos,
            @LocalVariable(ordinal = 0) RenderChunkRegion region,
            @LocalVariable(ordinal = 0) SectionBufferBuilderPack pack,
            @LocalVariable(ordinal = 0) Map<RenderType, BufferBuilder> map,
            @LocalVariable(ordinal = 0) PoseStack poseStack
    ) {
        RenderChunkRegionExtension extension = (RenderChunkRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        boolean shaded = extension.shadeSchematicaBlocks_CU();
        RandomSource random = RandomSource.create();
        BlockAndTintGetter wrapped = extension.asWrapped_CU();

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

            FluidState fluidstate = state.getFluidState();
            if (!fluidstate.isEmpty()) {
                RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                VertexConsumer consumer = this.getOrBeginLayer_CU(map, pack, layer, shaded);
                this.blockRenderer.renderLiquid(pos, wrapped, consumer, state, fluidstate);
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                BakedModel model = this.blockRenderer.getBlockModel(state);
                ModelData modelData = wrapped.getModelData(pos);
                modelData = model.getModelData(wrapped, pos, state, modelData);
                random.setSeed(state.getSeed(pos));
                for (RenderType layer : model.getRenderTypes(state, random, modelData)) {
                    VertexConsumer consumer = this.getOrBeginLayer_CU(map, pack, layer, shaded);
                    poseStack.pushPose();
                    poseStack.translate(
                            SectionPos.sectionRelative(pos.getX()),
                            SectionPos.sectionRelative(pos.getY()),
                            SectionPos.sectionRelative(pos.getZ()));
                    this.blockRenderer.renderBatched(state, pos, wrapped, poseStack, consumer, true, random, modelData, layer);
                    poseStack.popPose();
                }
            }
        }
    }

    @Unique
    private VertexConsumer getOrBeginLayer_CU(
            Map<RenderType, BufferBuilder> map,
            SectionBufferBuilderPack pack,
            RenderType layer,
            boolean shaded
    ) {
        VertexConsumer consumer = this.getOrBeginLayer(map, pack, layer);
        return shaded ? new ShadedVertexConsumerWrapper(consumer) : consumer;
    }
}