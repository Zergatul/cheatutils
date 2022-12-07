package com.zergatul.cheatutils.mixins;

import com.mojang.authlib.GameProfile;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.controllers.PlayerMotionController;
import com.zergatul.cheatutils.controllers.ZoomController;
import com.zergatul.cheatutils.helpers.MixinLocalPlayerHelper;
import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    private boolean flyHackOverride = false;
    private boolean oldFlying;
    private float oldFlyingSpeed;

    public MixinLocalPlayer(ClientLevel p_234112_, GameProfile p_234113_, @Nullable ProfilePublicKey p_234114_) {
        super(p_234112_, p_234113_, p_234114_);
    }

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
        MixinLocalPlayerHelper.insideAiStep = true;

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
        MixinLocalPlayerHelper.insideAiStep = false;

        if (flyHackOverride) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            player.getAbilities().flying = oldFlying;
            player.getAbilities().setFlyingSpeed(oldFlyingSpeed);
            flyHackOverride = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> info) {
        if (MixinLocalPlayerHelper.insideAiStep) {
            if (ConfigStore.instance.getConfig().movementHackConfig.disableSlowdownOnUseItem) {
                info.setReturnValue(false);
            }
        }
    }

    @ModifyArg(
            method = "Lnet/minecraft/client/player/LocalPlayer;aiStep()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"),
            index = 0
    )
    private boolean onAiStepInputTick(boolean isMovingSlowly) {
        var config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.disableCrouchingSlowdown) {
            return false;
        }
        return isMovingSlowly;
    }

    @Override
    public void lerpMotion(double dx, double dy, double dz) {
        var config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.antiKnockback) {
            return;
        }
        super.lerpMotion(dx, dy, dz);
    }

    @Override
    public void push(double dx, double dy, double dz) {
        var config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.antiPush) {
            return;
        }
        super.push(dx, dy, dz);
    }

    @Override
    protected float getJumpPower() {
        MovementHackConfig config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.scaleJumpHeight) {
            return (float) (Math.sqrt(config.jumpHeightFactor) * super.getJumpPower());
        } else {
            return super.getJumpPower();
        }
    }

    @Override
    public void turn(double yRot, double xRot) {
        if (!MixinMouseHandlerHelper.insideTurnPlayer) {
            return;
        }
        if (FreeCamController.instance.isActive()) {
            FreeCamController.instance.onMouseTurn(yRot, xRot);
            return;
        } else {
            if (ConfigStore.instance.getConfig().lockInputsConfig.mouseInputDisabled) {
                return;
            }
        }

        if (ZoomController.instance.isActive()) {
            xRot *= ZoomController.instance.getFovFactor();
            yRot *= ZoomController.instance.getFovFactor();
        }

        super.turn(yRot, xRot);
    }
}