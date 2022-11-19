package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/render/GameRenderer;updateTargetedEntity(F)V")
    private void onBeforeUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        MixinGameRendererHelper.insideUpdateTargetedEntity = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/render/GameRenderer;updateTargetedEntity(F)V")
    private void onAfterUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        MixinGameRendererHelper.insideUpdateTargetedEntity = false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"), method = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", require = 0)
    private void onRenderItemInHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo info) {
        MixinGameRendererHelper.insideRenderItemInHand = true;
    }
}