package com.zergatul.cheatutils.mixins;

import com.mojang.authlib.GameProfile;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.controllers.PlayerMotionController;
import com.zergatul.cheatutils.helpers.MixinLocalPlayerHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    private boolean flyHackOverride = false;
    private boolean oldFlying;
    private float oldFlyingSpeed;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, profile, publicKey);
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V")
    private void onBeforeSendPosition(CallbackInfo info) {
        PlayerMotionController.instance.triggerOnBeforeSendPosition();
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V")
    private void onAfterSendPosition(CallbackInfo info) {
        PlayerMotionController.instance.triggerOnAfterSendPosition();
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/network/ClientPlayerEntity;tickMovement()V")
    private void onBeforeAiStep(CallbackInfo info) {
        MixinLocalPlayerHelper.insideAiStep = true;

        FlyHackConfig config = ConfigStore.instance.getConfig().flyHackConfig;
        if (config.enabled) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

            oldFlying = player.getAbilities().flying;
            oldFlyingSpeed = player.getAbilities().getFlySpeed();

            player.getAbilities().flying = true;
            if (config.overrideFlyingSpeed) {
                player.getAbilities().setFlySpeed(config.flyingSpeed);
            }

            flyHackOverride = true;
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/network/ClientPlayerEntity;tickMovement()V")
    private void onAfterAiStep(CallbackInfo info) {
        MixinLocalPlayerHelper.insideAiStep = false;

        if (flyHackOverride) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            player.getAbilities().flying = oldFlying;
            player.getAbilities().setFlySpeed(oldFlyingSpeed);
            flyHackOverride = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", cancellable = true)
    private void onIsUsingItem(CallbackInfoReturnable<Boolean> info) {
        if (MixinLocalPlayerHelper.insideAiStep) {
            if (ConfigStore.instance.getConfig().movementHackConfig.disableSlowdownOnUseItem) {
                info.setReturnValue(false);
            }
        }
    }

    @ModifyArg(
            method = "Lnet/minecraft/client/network/ClientPlayerEntity;tickMovement()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(ZF)V"),
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
    public void setVelocityClient(double dx, double dy, double dz) {
        var config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.antiKnockback) {
            return;
        }
        super.setVelocityClient(dx, dy, dz);
    }

    @Override
    public void addVelocity(double dx, double dy, double dz) {
        var config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.antiPush) {
            return;
        }
        super.addVelocity(dx, dy, dz);
    }
}