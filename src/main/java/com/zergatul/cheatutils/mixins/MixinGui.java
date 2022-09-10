package com.zergatul.cheatutils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.helpers.MixinGuiHelper;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void onBeforeRenderCrosshair(PoseStack posestack, CallbackInfo info) {
        MixinGuiHelper.insideRenderCrosshair = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void onAfterRenderCrosshair(PoseStack posestack, CallbackInfo info) {
        MixinGuiHelper.insideRenderCrosshair = false;
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/Gui;renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;)V", cancellable = true)
    private void onRenderEffects(PoseStack p_93029_, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            info.cancel();
        }
    }
}