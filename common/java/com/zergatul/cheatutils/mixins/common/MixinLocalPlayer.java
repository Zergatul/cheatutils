package com.zergatul.cheatutils.mixins.common;

import com.mojang.authlib.GameProfile;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.controllers.PlayerMotionController;
import com.zergatul.cheatutils.helpers.MixinLocalPlayerHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
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

    public MixinLocalPlayer(ClientLevel p_234112_, GameProfile p_234113_) {
        super(p_234112_, p_234113_, null);
    }

    @Inject(at = @At("HEAD"), method = "sendPosition()V")
    private void onBeforeSendPosition(CallbackInfo info) {
        PlayerMotionController.instance.triggerOnBeforeSendPosition();
    }

    @Inject(at = @At("TAIL"), method = "sendPosition()V")
    private void onAfterSendPosition(CallbackInfo info) {
        PlayerMotionController.instance.triggerOnAfterSendPosition();
    }

    @Inject(at = @At("HEAD"), method = "aiStep()V")
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

    @Inject(at = @At("TAIL"), method = "aiStep()V")
    private void onAfterAiStep(CallbackInfo info) {
        MixinLocalPlayerHelper.insideAiStep = false;

        if (flyHackOverride) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            player.getAbilities().flying = oldFlying;
            player.getAbilities().setFlyingSpeed(oldFlyingSpeed);
            flyHackOverride = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "isUsingItem()Z", cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> info) {
        if (MixinLocalPlayerHelper.insideAiStep) {
            if (ConfigStore.instance.getConfig().movementHackConfig.disableSlowdownOnUseItem) {
                info.setReturnValue(false);
            }
        }
    }

    @ModifyArg(
            method = "aiStep()V",
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
}