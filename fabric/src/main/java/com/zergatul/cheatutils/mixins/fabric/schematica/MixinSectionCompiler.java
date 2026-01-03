package com.zergatul.cheatutils.mixins.fabric.schematica;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
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

import java.util.List;
import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> map, SectionBufferBuilderPack pack, ChunkSectionLayer layer);

    @Inject(
            method = "compile",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private void onBeforeSorting(
            SectionPos sectionPos,
            RenderSectionRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack pack,
            CallbackInfoReturnable<SectionCompiler.Results> info,
            @Local(ordinal = 0) Map<ChunkSectionLayer, BufferBuilder> map,
            @Local(ordinal = 0) PoseStack poseStack
    ) {
        RenderSectionRegionExtension extension = (RenderSectionRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        boolean shaded = extension.shadeSchematicaBlocks_CU();
        RandomSource random = RandomSource.create();
        List<BlockModelPart> list = new ObjectArrayList<>();
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
                ChunkSectionLayer layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
                this.blockRenderer.renderLiquid(pos, wrapped, getOrBeginLayer_CU(map, pack, layer, shaded), state, fluidState);
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                ChunkSectionLayer layer = ItemBlockRenderTypes.getChunkRenderType(state);
                random.setSeed(state.getSeed(pos));
                this.blockRenderer.getBlockModel(state).collectParts(random, list);
                poseStack.pushPose();
                poseStack.translate(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
                this.blockRenderer.renderBatched(state, pos, wrapped, poseStack, getOrBeginLayer_CU(map, pack, layer, shaded), true, list);
                poseStack.popPose();
                list.clear();
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