package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.controllers.PlayerMotionController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {

    private boolean flyHackOverride = false;
    private boolean oldFlying;
    private float oldFlyingSpeed;

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V")
    private void onBeforeSendPosition(CallbackInfo info) {
        PlayerMotionController.instance.triggerOnBeforeSendPosition();
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V")
    private void onAfterSendPosition(CallbackInfo info) {
        PlayerMotionController.instance.triggerOnAfterSendPosition();
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/player/LocalPlayer;aiStep()V")
    private void onBeforeAiStep(CallbackInfo info) {
        FlyHackConfig config = ConfigStore.instance.getConfig().flyHackConfig;
        if (config.enabled) {
            LocalPlayer player = (LocalPlayer) (Object) this;

            oldFlying = player.getAbilities().flying;
            oldFlyingSpeed = player.getAbilities().getFlyingSpeed();

            player.getAbilities().flying = true;
            if (config.overrideFlyingSpeed) {
                player.getAbilities().setFlyingSpeed(config.flyingSpeed);
            }

            flyHackOverride = true;
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/player/LocalPlayer;aiStep()V")
    private void onAfterAiStep(CallbackInfo info) {
        if (flyHackOverride) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            player.getAbilities().flying = oldFlying;
            player.getAbilities().setFlyingSpeed(oldFlyingSpeed);
            flyHackOverride = false;
        }
    }
}
