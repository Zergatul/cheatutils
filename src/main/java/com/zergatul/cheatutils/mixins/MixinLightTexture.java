package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.FullBrightController;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class MixinLightTexture {

    @Inject(at = @At("HEAD"), method = "update(F)V")
    public void onBeforeUpdateLightTexture(float f5, CallbackInfo info) {
        FullBrightController.instance.insideUpdateLightTexture = true;
    }

    @Inject(at = @At("TAIL"), method = "update(F)V")
    public void onAfterUpdateLightTexture(float f5, CallbackInfo info) {
        FullBrightController.instance.insideUpdateLightTexture = false;
    }
}