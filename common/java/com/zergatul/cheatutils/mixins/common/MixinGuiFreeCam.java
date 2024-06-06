package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Gui.class, priority = 2000)
public abstract class MixinGuiFreeCam {

    @ModifyExpressionValue(
            method = "renderCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean onRenderCrosshairIsFirstPerson(boolean isFirstPerson) {
        return FreeCam.instance.onRenderCrosshairIsFirstPerson(isFirstPerson);
    }
}