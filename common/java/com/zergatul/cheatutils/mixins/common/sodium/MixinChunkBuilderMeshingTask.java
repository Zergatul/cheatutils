package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.SodiumBlockRenderCacheExtension;
import com.zergatul.cheatutils.extensions.SodiumBlockRendererExtension;
import com.zergatul.cheatutils.extensions.SodiumWorldSliceExtension;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class MixinChunkBuilderMeshingTask {

    @Shadow(remap = false)
    @Final
    private ChunkRenderContext renderContext;

    @Inject(
            method = "execute",
            at = @At(value = "NEW", target = "Lit/unimi/dsi/fastutil/objects/Reference2ReferenceOpenHashMap;", remap = false),
            remap = false)
    private void onBeforeBuildMeshes(
            ChunkBuildContext buildContext,
            CancellationToken cancellationToken,
            CallbackInfoReturnable<ChunkBuildOutput> info
    ) {
        BlockRenderCache cache = buildContext.cache;
        WorldSlice slice = cache.getWorldSlice();
        SodiumWorldSliceExtension sliceExtension = (SodiumWorldSliceExtension) (Object) slice;
        if (!sliceExtension.hasSchematicaBlocks_CU()) {
            return;
        }

        SectionPos sectionPos = this.renderContext.getOrigin();
        ((SodiumBlockRenderCacheExtension) cache).resetLightDataCache_CU(
                sectionPos.getX(), sectionPos.getY(), sectionPos.getZ());

        BlockRenderer blockRenderer = cache.getBlockRenderer();
        FluidRenderer fluidRenderer = cache.getFluidRenderer();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos modelOffset = new BlockPos.MutableBlockPos();
        BlockRenderContext context = new BlockRenderContext(slice);

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
                                context.update(blockPos, modelOffset, state, model, state.getSeed(blockPos));
                                blockRenderer.renderModel(context, buildContext.buffers);
                            }

                            FluidState fluidState = state.getFluidState();
                            if (!fluidState.isEmpty()) {
                                fluidRenderer.render(slice, fluidState, blockPos, modelOffset, buildContext.buffers);
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