package com.zergatul.cheatutils.mixins.neoforge.schematics;

import com.llamalad7.mixinextras.sugar.Local;
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
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Function;

@Mixin(SectionCompiler.class)
public abstract class MixinSectionCompiler {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Inject(
            method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private void onBeforeSorting(
            SectionPos sectionPos,
            RenderSectionRegion region,
            VertexSorting sorting,
            SectionBufferBuilderPack pack,
            List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers,
            CallbackInfoReturnable<SectionCompiler.Results> info,
            @Local(ordinal = 0) Function<ChunkSectionLayer, VertexConsumer> bufferLookup,
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

        bufferLookup = wrapBufferLookup_CU(bufferLookup, shaded);

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
                this.blockRenderer.renderLiquid(pos, wrapped, bufferLookup.apply(layer), state, fluidState);
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                random.setSeed(state.getSeed(pos));
                this.blockRenderer.getBlockModel(state).collectParts(region, pos, state, random, list);
                poseStack.pushPose();
                poseStack.translate(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
                this.blockRenderer.renderBatched(state, pos, wrapped, poseStack, bufferLookup, true, list);
                poseStack.popPose();
                list.clear();
            }
        }
    }

    @Unique
    private Function<ChunkSectionLayer, VertexConsumer> wrapBufferLookup_CU(Function<ChunkSectionLayer, VertexConsumer> bufferLookup, boolean shaded) {
        if (shaded) {
            return layer -> new ShadedVertexConsumerWrapper(bufferLookup.apply(layer));
        } else {
            return bufferLookup;
        }
    }
}