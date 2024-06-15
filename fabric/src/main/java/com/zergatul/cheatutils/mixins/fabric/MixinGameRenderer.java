package com.zergatul.cheatutils.mixins.fabric;

import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(at = @At("HEAD"), method = "renderLevel")
    private void onBeforeRenderLevel(DeltaTracker delta, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = true;
    }

    @Inject(at = @At("TAIL"), method = "renderLevel")
    private void onAfterRenderLevel(DeltaTracker delta, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = false;
    }
}