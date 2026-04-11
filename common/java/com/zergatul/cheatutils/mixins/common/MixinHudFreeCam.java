package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Hud.class, priority = 2000)
public abstract class MixinHudFreeCam {

    @ModifyMethodReturnValue(
            method = "extractCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private static boolean onRenderCrosshairIsFirstPerson(boolean isFirstPerson) {
        return FreeCam.instance.onRenderCrosshairIsFirstPerson(isFirstPerson);
    }
}