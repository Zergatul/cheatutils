package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.cheatutils.schematics.extensions.SectionCompileInfo;
import com.zergatul.cheatutils.schematics.extensions.SectionCompilerExtension;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionRegion.class)
public abstract class MixinRenderSectionRegion {

    @Inject(at = @At("RETURN"), method = "getBlockState", cancellable = true)
    private void onGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> info) {
        if (info.getReturnValue().isAir()) {
            SectionCompileInfo sectionInfo = SectionCompilerExtension.COMPILE_INFO.get();
            if (sectionInfo.shouldRenderSchematicaGhostBlocks) {
                BlockState state = sectionInfo.schematicaSectionInfo.contains(pos) ?
                        sectionInfo.schematicaSectionInfo.getBlockState(pos) :
                        Schematica.instance.getBlockState(pos);
                if (!state.isAir()) {
                    info.setReturnValue(state);
                }
            }
        }
    }
}