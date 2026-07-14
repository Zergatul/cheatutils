package com.zergatul.cheatutils.mixins.common.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.extensions.SodiumBlockRenderCacheExtension;
import com.zergatul.cheatutils.extensions.SodiumLevelSliceExtension;
import com.zergatul.cheatutils.schematics.SodiumSchematicaRendering;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
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

@Mixin(ChunkBuilderMeshingTask.class)
public abstract class MixinChunkBuilderMeshingTask {

    @Shadow
    @Final
    private ChunkRenderContext renderContext;

    @Inject(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformLevelRenderHooks;runChunkMeshAppenders(Ljava/util/List;Ljava/util/function/Function;Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true)
    private void onBeforeMeshAppenders(
            ChunkBuildContext buildContext,
            CancellationToken cancellationToken,
            CallbackInfoReturnable<ChunkBuildOutput> info,
            @Local TranslucentGeometryCollector collector
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

        for (int y = minY; y < maxY; y++) {
            if (cancellationToken.isCancelled()) {
                info.setReturnValue(null);
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
                            BlockStateModel model = cache.getBlockModels().get(state);
                            SodiumSchematicaRendering.begin(sliceExtension.shadeSchematicaBlocks_CU());
                            try {
                                blockRenderer.renderModel(model, state, blockPos, modelOffset);
                            } finally {
                                SodiumSchematicaRendering.end();
                            }
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
    }
}