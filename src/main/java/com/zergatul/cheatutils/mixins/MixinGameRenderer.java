package com.zergatul.cheatutils.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V")
    private void onBeforePick(float vec33, CallbackInfo info) {
        MixinGameRendererHelper.insidePick = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V")
    private void onAfterPick(float vec33, CallbackInfo info) {
        MixinGameRendererHelper.insidePick = false;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/PointOfView;isFirstPerson()Z"), method = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/ActiveRenderInfo;F)V")
    private void onRenderItemInHand(MatrixStack p_228381_1_, ActiveRenderInfo p_228381_2_, float p_228381_3_, CallbackInfo info) {
        MixinGameRendererHelper.insideRenderItemInHand = true;
    }
}