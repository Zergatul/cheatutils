package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.extensions.BlockRendererExtension;
import com.zergatul.cheatutils.extensions.LevelSliceExtension;
import com.zergatul.mixin.LiteInject;
import com.zergatul.mixin.LocalVariable;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class MixinChunkBuilderMeshingTask {

//    @LiteInject(
//            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V",
//                    shift = At.Shift.BEFORE))
//    private void onBeforeCallRenderModel(
//            @LocalVariable(ordinal = 0) LevelSlice slice,
//            @LocalVariable(ordinal = 0) BlockRenderer blockRenderer,
//            @LocalVariable(ordinal = 8) int x,
//            @LocalVariable(ordinal = 6) int y,
//            @LocalVariable(ordinal = 7) int z
//    ) {
//        if (((LevelSliceExtension) slice).hasSchematicaBlockAt_CU(x, y, z)) {
//            ((BlockRendererExtension) blockRenderer).setSchematicaShadeMode_CU(true);
//        }
//    }
//
//    @LiteInject(
//            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/renderer/block/model/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"))
//    private void onAfterCallRenderModel(
//            @LocalVariable(ordinal = 0) BlockRenderer blockRenderer
//    ) {
//        ((BlockRendererExtension) blockRenderer).setSchematicaShadeMode_CU(false);
//    }
}