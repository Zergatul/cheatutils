package com.zergatul.cheatutils.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.zergatul.cheatutils.helpers.MixinGuiHelper;
import net.minecraft.client.gui.IngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IngameGui.class)
public abstract class MixinIngameGui {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/IngameGui;renderCrosshair(Lcom/mojang/blaze3d/matrix/MatrixStack;)V")
    private void onBeforeRenderCrosshair(MatrixStack posestack, CallbackInfo info) {
        MixinGuiHelper.insideRenderCrosshair = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/IngameGui;renderCrosshair(Lcom/mojang/blaze3d/matrix/MatrixStack;)V")
    private void onAfterRenderCrosshair(MatrixStack posestack, CallbackInfo info) {
        MixinGuiHelper.insideRenderCrosshair = false;
    }
}