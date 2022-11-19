package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import com.zergatul.cheatutils.helpers.MixinInGameHudHelper;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Perspective.class)
public abstract class MixinPerspective {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z", cancellable = true)
    private void onIsFirstPerson(CallbackInfoReturnable<Boolean> info) {
        if (!FreeCamController.instance.isActive()) {
            MixinGameRendererHelper.insideRenderItemInHand = false;
            return;
        }
        if (MixinInGameHudHelper.insideRenderCrosshair) {
            info.setReturnValue(true);
            info.cancel();
            return;
        }
        if (MixinGameRendererHelper.insideRenderItemInHand) {
            MixinGameRendererHelper.insideRenderItemInHand = false;
            if (ConfigStore.instance.getConfig().freeCamConfig.renderHands) {
                info.setReturnValue(true);
            }
        }
    }
}