package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.interfaces.GameRendererMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements GameRendererMixinInterface {

    @Shadow
    protected abstract double getFov(Camera camera, float partialTicks, boolean usedConfiguredFov);

    @Inject(at = @At("HEAD"), method = "pick(F)V")
    private void onBeforePick(float vec33, CallbackInfo info) {
        FreeCamController.instance.onBeforeGameRendererPick();
    }

    @Inject(at = @At("TAIL"), method = "pick(F)V")
    private void onAfterPick(float vec33, CallbackInfo info) {
        FreeCamController.instance.onAfterGameRendererPick();
    }

    @Redirect(
            method = "renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", ordinal = 0))
    private boolean onRenderItemInHandIsFirstPerson(CameraType cameraType) {
        return FreeCamController.instance.onRenderItemInHandIsFirstPerson(cameraType);
    }

    @Override
    public double getFov(Camera camera, float partialTicks) {
        return getFov(camera, partialTicks, true);
    }
}