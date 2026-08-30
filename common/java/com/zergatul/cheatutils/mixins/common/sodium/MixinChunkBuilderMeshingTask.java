package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.SodiumBlockRenderCacheExtension;
import com.zergatul.cheatutils.extensions.SodiumBlockRendererExtension;
import com.zergatul.cheatutils.extensions.SodiumLevelSliceExtension;
import com.zergatul.mixin.LiteInject;
import com.zergatul.mixin.LocalVariable;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class MixinChunkBuilderMeshingTask {

    @Shadow(remap = false)
    @Final
    private ChunkRenderContext renderContext;

    @LiteInject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformLevelRenderHooks;runChunkMeshAppenders(Ljava/util/List;Ljava/util/function/Function;Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;)V",
                    remap = false))
    private void onBeforeMeshAppenders(
            @LocalVariable(ordinal = 0) ChunkBuildContext buildContext,
            @LocalVariable(ordinal = 0) CancellationToken cancellationToken,
            @LocalVariable(ordinal = 0) TranslucentGeometryCollector collector
    ) {
        BlockRenderCache cache = buildContext.cache;
        LevelSlice slice = cache.getWorldSlice();
        SodiumLevelSliceExtension sliceExtension = (SodiumLevelSliceExtension) (Object) slice;
        if (!sliceExtension.hasSchematicaBlocks_CU()) {
            return;
        }

        SectionPos sectionPos = this.renderContext.getOrigin();
        ((SodiumBlockRenderCacheExtension) cache).resetLightDataCache_CU(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ());

        BlockRenderer blockRenderer = cache.getBlockRenderer();
        FluidRenderer fluidRenderer = cache.getFluidRenderer();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos modelOffset = new BlockPos.MutableBlockPos();

        int minX = sectionPos.minBlockX();
        int minY = sectionPos.minBlockY();
        int minZ = sectionPos.minBlockZ();
        int maxX = minX + 16;
        int maxY = minY + 16;
        int maxZ = minZ + 16;

        ((SodiumBlockRendererExtension) blockRenderer).setShaded_CU(sliceExtension.shadeSchematicaBlocks_CU());
        try {
            for (int y = minY; y < maxY; y++) {
                if (cancellationToken.isCancelled()) {
                    return;
                }

                for (int z = minZ; z < maxZ; z++) {
                    for (int x = minX; x < maxX; x++) {
                        if (!slice.getBlockState(x, y, z).isAir()) {
                            continue;
                        }

                        BlockState state = sliceExtension.getSchematicaBlockState_CU(x, y, z);
                        if (state.isAir()) {
                            continue;
                        }

                        blockPos.set(x, y, z);
                        modelOffset.set(x & 15, y & 15, z & 15);
                        sliceExtension.setSchematicaView_CU(true);
                        try {
                            if (state.getRenderShape() == RenderShape.MODEL) {
                                BakedModel model = cache.getBlockModels().getBlockModel(state);
                                blockRenderer.renderModel(model, state, blockPos, modelOffset);
                            }

                            FluidState fluidState = state.getFluidState();
                            if (!fluidState.isEmpty()) {
                                fluidRenderer.render(slice, state, fluidState, blockPos, modelOffset, collector, buildContext.buffers);
                            }
                        } finally {
                            sliceExtension.setSchematicaView_CU(false);
                        }
                    }
                }
            }
        } finally {
            ((SodiumBlockRendererExtension) blockRenderer).setShaded_CU(false);
        }
    }
}