package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class MixinLightTexture {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/renderer/LightTexture;updateLightTexture(F)V")
    public void onBeforeUpdateLightTexture(float f5, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/LightTexture;updateLightTexture(F)V")
    public void onAfterUpdateLightTexture(float f5, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = false;
    }
}