package com.zergatul.cheatutils.mixins.common.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.modules.automation.Schematica;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {

    @ModifyExpressionValue(
            method = "onSectionAdded",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;hasOnlyAir()Z"))
    private boolean onCheckSectionEmpty(
            boolean original,
            @Local(argsOnly = true, ordinal = 0) int x,
            @Local(argsOnly = true, ordinal = 1) int y,
            @Local(argsOnly = true, ordinal = 2) int z
    ) {
        Schematica schematica = Schematica.instance;
        return original && (!schematica.isBlockRenderingEnabled() || !schematica.hasBlocksAtSection(x, y, z));
    }
}