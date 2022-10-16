package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import com.zergatul.cheatutils.helpers.MixinGuiHelper;
import net.minecraft.client.settings.PointOfView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PointOfView.class)
public class MixinPointOfView {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/settings/PointOfView;isFirstPerson()Z", cancellable = true)
    private void onIsFirstPerson(CallbackInfoReturnable<Boolean> info) {
        if (!FreeCamController.instance.isActive()) {
            return;
        }
        if (MixinGuiHelper.insideRenderCrosshair) {
            info.setReturnValue(true);
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