package com.zergatul.cheatutils.mixins.forge.schematica;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.zergatul.cheatutils.extensions.RenderSectionRegionExtension;
import com.zergatul.cheatutils.schematics.ShadedVertexConsumerWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
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
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.*;
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
            @Local(ordinal = 1) Map<ChunkSectionLayer, BufferBuilder> map,
            @Local(ordinal = 0) PoseStack poseStack
    ) {
        RenderSectionRegionExtension extension = (RenderSectionRegionExtension) region;
        if (!extension.hasSchematicaBlocks_CU()) {
            return;
        }

        ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        assert level != null;
        Map<BlockPos, ModelData> modelDataMap = level.getModelDataManager().getAt(sectionPos);

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
                BlockStateModel model = this.blockRenderer.getBlockModel(state);
                ModelData data = modelDataMap.getOrDefault(pos, net.minecraftforge.client.model.data.ModelData.EMPTY);
                data = model.getModelData(wrapped, pos, state, data);
                random.setSeed(state.getSeed(pos));
                for (ChunkSectionLayer layer : model.getRenderTypes(state, random, data)) {
                    VertexConsumer consumer = this.getOrBeginLayer_CU(map, pack, layer, shaded);
                    random.setSeed(state.getSeed(pos));
                    model.collectParts(random, list, data, layer);
                    poseStack.pushPose();
                    poseStack.translate(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
                    this.blockRenderer.renderBatched(state, pos, wrapped, poseStack, consumer, true, list);
                    poseStack.popPose();
                    list.clear();
                }
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