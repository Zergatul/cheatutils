package com.zergatul.cheatutils.mixins.common;

import com.mojang.authlib.GameProfile;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.modules.hacks.ElytraFly;
import com.zergatul.cheatutils.modules.scripting.Debugging;
import com.zergatul.cheatutils.modules.utilities.Privacy;
import com.zergatul.mixin.ExecuteAfterIfElseCondition;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    @Shadow
    @Final
    public ClientPacketListener connection;

    public MixinLocalPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    @Inject(at = @At("HEAD"), method = "sendPosition()V")
    private void onBeforeSendPosition(CallbackInfo info) {
        Events.BeforeSendPlayerPos.trigger();
    }

    @ExecuteAfterIfElseCondition(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z"))
    private void onAfterSendPosition() {
        Events.AfterSendPlayerPos.trigger();
    }

    @Inject(at = @At("HEAD"), method = "aiStep()V")
    private void onBeforeAiStep(CallbackInfo info) {
        Events.BeforePlayerAiStep.trigger();
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onInput(Lnet/minecraft/client/player/ClientInput;)V"))
    private void onModifyPlayerInput(CallbackInfo info) {
        Events.ModifyPlayerInput.trigger();
    }

    @Inject(at = @At("TAIL"), method = "aiStep()V")
    private void onAfterAiStep(CallbackInfo info) {
        Events.AfterPlayerAiStep.trigger();
    }

    @Inject(at = @At("HEAD"), method = "itemUseSpeedMultiplier", cancellable = true)
    private void onBeforeGetItemUseSpeedMultiplier(CallbackInfoReturnable<Float> info) {
        if (ConfigStore.instance.getConfig().movementHackConfig.disableSlowdownOnUseItem) {
            info.setReturnValue(1f);
        }
    }

    @ModifyMethodReturnValue(
            method = "modifyInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z"))
    private static boolean onAiStepCrouchCheck(boolean isMovingSlowly) {
        if (ConfigStore.instance.getConfig().movementHackConfig.disableCrouchingSlowdown) {
            return false;
        } else {
            return isMovingSlowly;
        }
    }

    @ModifyConstant(method = "aiStep()V", constant = @Constant(floatValue = 3.0f))
    private float onModifyFlyingHorizontalMultiplier(float value) {
        return ElytraFly.instance.onModifyFlyingHorizontalMultiplier(value);
    }

    @Override
    public void lerpMotion(Vec3 movement) {
        MovementHackConfig config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.antiKnockback) {
            return;
        }
        super.lerpMotion(movement);
    }

    @Override
    public void push(double dx, double dy, double dz) {
        MovementHackConfig config = ConfigStore.instance.getConfig().movementHackConfig;
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

    @ModifyArgs(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void onModifyDeltaMove(Args args) {
        MoverType type = args.get(0);
        Vec3 delta = args.get(1);
        if (type == MoverType.SELF) {
            args.set(1, ElytraFly.instance.onModifyDeltaMove(delta));
        }
    }

    @Inject(at = @At("HEAD"), method = "openTextEdit", cancellable = true)
    private void onOpenTextEdit(SignBlockEntity sign, boolean isFrontText, CallbackInfo info) {
        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        Privacy privacy = Privacy.instance;

        if (config.logTranslationExploitDetails) {
            privacy.forEachExploitable(
                    sign,
                    translatable -> Debugging.instance.addMessage("Translatable exploit attempt. Key=" + translatable.getKey()),
                    keybind -> Debugging.instance.addMessage("Keybind exploit attempt. Key=" + keybind.getName()));
        }

        if (!privacy.shouldDisconnectOnTranslationExploit(config)) {
            return;
        }

        Optional.<Component>empty()
                .or(() -> privacy.checkExploitable(sign.getBackText()))
                .or(() -> privacy.checkExploitable(sign.getFrontText()))
                .ifPresent(reason -> {
                    this.connection.getConnection().disconnect(reason);
                    info.cancel();
                });
    }

    @Override
    public boolean isPushedByFluid() {
        MovementHackConfig config = ConfigStore.instance.getConfig().movementHackConfig;
        if (config.disableWaterPush) {
            return false;
        } else {
            return super.isPushedByFluid();
        }
    }

    @Override
    public double blockInteractionRange() {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideReachDistance) {
            return config.reachDistance;
        } else {
            return super.blockInteractionRange();
        }
    }

    @Override
    public double entityInteractionRange() {
        ReachConfig config = ConfigStore.instance.getConfig().reachConfig;
        if (config.overrideAttackRange) {
            return config.attackRange;
        } else {
            return super.entityInteractionRange();
        }
    }

    @Override
    public float maxUpStep() {
        StepUpConfig config = ConfigStore.instance.getConfig().stepUp;
        if (config.enabled) {
            return (float) config.height;
        } else {
            return super.maxUpStep();
        }
    }
}