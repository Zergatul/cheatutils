package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraTunnelConfig;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinEntityHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow(aliases = "Lnet/minecraft/world/entity/Entity;calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;")
    protected abstract Vec3 calculateViewVector(float p_20172_, float p_20173_);

    @Inject(at = @At("HEAD"), method = "getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        var entity = (Entity) (Object) this;
        for (EntityTracerConfig config : ConfigStore.instance.getConfig().entities.configs) {
            if (config.enabled && config.isValidEntity(entity) && config.glow) {
                info.setReturnValue(config.glowColor.getRGB());
                info.cancel();
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onGetEyePosition(float p_20300_, CallbackInfoReturnable<Vec3> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.shouldOverrideCameraEntityPosition((Entity) (Object) this)) {
            info.setReturnValue(new Vec3(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getViewVector(F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onGetViewVector(float p_20253_, CallbackInfoReturnable<Vec3> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.shouldOverrideCameraEntityPosition((Entity) (Object) this)) {
            info.setReturnValue(this.calculateViewVector(freeCam.getXRot(), freeCam.getYRot()));
        }
    }

    @Inject(at = @At("TAIL"), method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onCollide(Vec3 vec31, CallbackInfoReturnable<Vec3> info) {
        ElytraTunnelConfig config = ConfigStore.instance.getConfig().elytraTunnelConfig;
        if (config.enabled) {
            Entity entity = (Entity) (Object) this;
            if (entity instanceof LocalPlayer) {
                LocalPlayer player = (LocalPlayer) entity;
                if (player.isFallFlying()) {
                    //ModMain.LOGGER.info("collide: {} -> {}", vec31, info.getReturnValue());
                    Vec3 result = info.getReturnValue();
                    AABB aabb = player.getBoundingBox().expandTowards(result);
                    if (aabb.maxY > config.limit) {
                        info.setReturnValue(new Vec3(result.x, result.y - (aabb.maxY - config.limit), result.z));
                        //ModMain.LOGGER.info("collide override: {} -> {}", vec31, info.getReturnValue());
                        //ModMain.LOGGER.info("collide override: y={}, {} -> {}, maxy={}", player.getY(), vec31, info.getReturnValue(), aabb.maxY);
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "moveRelative(FLnet/minecraft/world/phys/Vec3;)V")
    private void onBeforeMoveRelative(float limit, Vec3 input, CallbackInfo info) {
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            MixinEntityHelper.insideMoveRelativeLocalPlayer = true;
        }
    }

    @Inject(at = @At("TAIL"), method = "moveRelative(FLnet/minecraft/world/phys/Vec3;)V")
    private void onAfterMoveRelative(float limit, Vec3 input, CallbackInfo info) {
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            MixinEntityHelper.insideMoveRelativeLocalPlayer = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private static void onGetInputVector(Vec3 vec3, float f, float f1, CallbackInfoReturnable<Vec3> info) {
        if (MixinEntityHelper.insideMoveRelativeLocalPlayer) {
            MovementHackConfig config = ConfigStore.instance.getConfig().movementHackConfig;
            if (config.scaleInputVector) {
                Vec3 vec = info.getReturnValue();
                info.setReturnValue(new Vec3(vec.x * config.inputVectorFactor, vec.y, vec.z * config.inputVectorFactor));
            }
        }
    }
}
