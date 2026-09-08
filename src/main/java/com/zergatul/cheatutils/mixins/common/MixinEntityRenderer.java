package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V"))
    private void onBeforeUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo info) {
        Events.BeforeRenderWorld.trigger();
    }

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V",
                    shift = At.Shift.AFTER))
    private void onAfterUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo info) {
        Events.AfterRenderWorld.trigger();
    }

    @Inject(at = @At("HEAD"), method = "getMouseOver(F)V")
    private void onBeforeGetMouseOver(float partialTicks, CallbackInfo info) {
        Events.OnBeforePick.trigger();
    }

    @Inject(at = @At("RETURN"), method = "getMouseOver(F)V")
    private void onAfterGetMouseOver(float partialTicks, CallbackInfo info) {
        Events.OnAfterPick.trigger();
    }

    @Inject(at = @At("HEAD"), method = "applyBobbing(F)V", cancellable = true)
    private void onApplyBobbing(float partialTicks, CallbackInfo info) {
        if (FreeCam.instance.shouldDisableBobbing()) {
            info.cancel();
        }
    }
}