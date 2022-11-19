package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinEntityHelper;
import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow(aliases = "Lnet/minecraft/entity/Entity;getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;")
    protected abstract Vec3d getRotationVector(float pitch, float yaw);

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getTeamColorValue()I", cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        var entity = (Entity) (Object) this;
        for (EntityTracerConfig config: ConfigStore.instance.getConfig().entities.configs) {
            if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                info.setReturnValue(config.glowColor.getRGB());
                info.cancel();
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;changeLookDirection(DD)V", cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo info) {
        if (!MixinMouseHandlerHelper.insideTurnPlayer) {
            return;
        }
        var entity = (Entity) (Object) this;
        if (!(entity instanceof ClientPlayerEntity)) {
            return;
        }
        if (FreeCamController.instance.isActive()) {
            FreeCamController.instance.onMouseTurn(yRot, xRot);
            info.cancel();
        } else {
            if (ConfigStore.instance.getConfig().lockInputsConfig.mouseInputDisabled) {
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getCameraPosVec(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetEyePosition(float p_20300_, CallbackInfoReturnable<Vec3d> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.isActive() && freeCam.shouldOverridePlayerPosition()) {
            var entity = (Entity) (Object) this;
            if (entity instanceof ClientPlayerEntity) {
                info.setReturnValue(new Vec3d(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetViewVector(float p_20253_, CallbackInfoReturnable<Vec3d> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.isActive() && freeCam.shouldOverridePlayerPosition()) {
            var entity = (Entity) (Object) this;
            if (entity instanceof ClientPlayerEntity) {
                info.setReturnValue(this.getRotationVector(freeCam.getXRot(), freeCam.getYRot()));
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V")
    private void onBeforeMoveRelative(float limit, Vec3d input, CallbackInfo info) {
        var entity = (Entity) (Object) this;
        if (entity instanceof ClientPlayerEntity) {
            MixinEntityHelper.insideMoveRelativeLocalPlayer = true;
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/entity/Entity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V")
    private void onAfterMoveRelative(float limit, Vec3d input, CallbackInfo info) {
        var entity = (Entity) (Object) this;
        if (entity instanceof ClientPlayerEntity) {
            MixinEntityHelper.insideMoveRelativeLocalPlayer = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private static void onGetInputVector(Vec3d vec3, float f, float f1, CallbackInfoReturnable<Vec3d> info) {
        if (MixinEntityHelper.insideMoveRelativeLocalPlayer) {
            MovementHackConfig config = ConfigStore.instance.getConfig().movementHackConfig;
            if (config.scaleInputVector) {
                Vec3d vec = info.getReturnValue();
                info.setReturnValue(new Vec3d(vec.x * config.inputVectorFactor, vec.y, vec.z * config.inputVectorFactor));
            }
        }
    }
}