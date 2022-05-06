package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.PlayerMotionController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V")
    private void onBeforeSendPosition(CallbackInfo ci) {
        PlayerMotionController.instance.triggerOnBeforeSendPosition();
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V")
    private void onAfterSendPosition(CallbackInfo ci) {
        PlayerMotionController.instance.triggerOnAfterSendPosition();
    }

}
