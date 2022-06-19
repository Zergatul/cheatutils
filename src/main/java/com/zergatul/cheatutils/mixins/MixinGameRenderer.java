package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V")
    private void onBeforePick(float vec33, CallbackInfo info) {
        MixinGameRendererHelper.insidePick = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V")
    private void onAfterPick(float vec33, CallbackInfo info) {
        MixinGameRendererHelper.insidePick = false;
    }
}
