package com.zergatul.cheatutils.mixins.common.schematics;

import com.zergatul.cheatutils.modules.automation.Schematica;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderRegionCache.class)
public abstract class MixinRenderRegionCache {

    @Unique
    private static SectionPos cheatutils$currentSectionPos;

    @Inject(at = @At("HEAD"), method = "createRegion")
    private void onCreateRegion(Level level, SectionPos sectionPos, CallbackInfoReturnable<RenderChunkRegion> info) {
        cheatutils$currentSectionPos = sectionPos;
    }

    @ModifyMethodReturnValue(
            method = "createRegion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;isSectionEmpty(I)Z"))
    private static boolean onCreateRegionCheckIfSectionIsEmpty(boolean isEmpty) {
        if (!isEmpty) {
            return false;
        }

        if (Schematica.instance.isBlockRenderingEnabled()) {
            return !Schematica.instance.hasBlocksAtSection(cheatutils$currentSectionPos);
        } else {
            return true;
        }
    }
}