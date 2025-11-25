package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.extensions.SectionCompileInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilderMeshingTask.class)
public abstract class MixinChunkBuilderMeshingTask extends ChunkBuilderTask<ChunkBuildOutput> {

    @Unique
    private SectionCompileInfo sectionCompileInfo_CU;

    @Inject(at = @At("HEAD"), method = "execute", remap = false)
    private void onBeforeExecute(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> info) {
        sectionCompileInfo_CU = new SectionCompileInfo();
        sectionCompileInfo_CU.sectionPos = SectionPos.of(this.render.getChunkX(), this.render.getChunkY(), this.render.getChunkZ());
        if (Schematica.instance.isBlockRenderingEnabled()) {
            Schematica.SectionInfo schematicaSectionInfo = Schematica.instance.getSectionInfo(sectionCompileInfo_CU.sectionPos);
            if (schematicaSectionInfo != null) {
                sectionCompileInfo_CU.schematicaSectionInfo = schematicaSectionInfo;
                sectionCompileInfo_CU.shouldRenderSchematicaGhostBlocks = true;
            } else {
                sectionCompileInfo_CU.shouldRenderSchematicaGhostBlocks = false;
            }
        } else {
            sectionCompileInfo_CU.shouldRenderSchematicaGhostBlocks = false;
        }
    }

    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onGetBlockState(LevelSlice slice, int blockX, int blockY, int blockZ) {
        BlockState original = slice.getBlockState(blockX, blockY, blockZ);
        if (original.isAir()) {
            if (sectionCompileInfo_CU.shouldRenderSchematicaGhostBlocks) {
                BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                BlockState state = sectionCompileInfo_CU.schematicaSectionInfo.contains(pos) ?
                        sectionCompileInfo_CU.schematicaSectionInfo.getBlockState(pos) :
                        Schematica.instance.getBlockState(pos);
                if (!state.isAir()) {
                    return state;
                }
            }
        }
        return original;
    }
}