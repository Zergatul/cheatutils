package com.zergatul.cheatutils.mixins.fabric.schematica;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.extensions.RenderChunkRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
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
    private BlockRenderDispatcher blockRenderer;

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(
            Map<RenderType, BufferBuilder> map,
            SectionBufferBuilderPack pack,
            RenderType layer);

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private void onBeforeSorting(
            SectionPos sectionPos,
            RenderChunkRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack pack,
            CallbackInfoReturnable<SectionCompiler.Results> info,
            @Local(ordinal = 0) Map<RenderType, BufferBuilder> map,
            @Local(ordinal = 0) PoseStack poseStack
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

            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty()) {
                RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
                this.blockRenderer.renderLiquid(
                        pos,
                        wrapped,
                        getOrBeginLayer_CU(map, pack, layer, shaded),
                        state,
                        fluidState);
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                RenderType layer = ItemBlockRenderTypes.getChunkRenderType(state);
                poseStack.pushPose();
                poseStack.translate(
                        SectionPos.sectionRelative(pos.getX()),
                        SectionPos.sectionRelative(pos.getY()),
                        SectionPos.sectionRelative(pos.getZ()));
                this.blockRenderer.renderBatched(
                        state,
                        pos,
                        wrapped,
                        poseStack,
                        getOrBeginLayer_CU(map, pack, layer, shaded),
                        true,
                        random);
                poseStack.popPose();
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