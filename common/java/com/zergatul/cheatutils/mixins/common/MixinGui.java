package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Redirect(
            method = "renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean onRenderCrosshairIsFirstPerson(CameraType cameraType) {
        return FreeCam.instance.onRenderCrosshairIsFirstPerson(cameraType);
    }

    @Inject(at = @At("HEAD"), method = "renderEffects(Lcom/mojang/blaze3d/vertex/PoseStack;)V", cancellable = true)
    private void onRenderEffects(PoseStack p_93029_, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().statusEffectsConfig.enabled) {
            info.cancel();
        }
    }
}