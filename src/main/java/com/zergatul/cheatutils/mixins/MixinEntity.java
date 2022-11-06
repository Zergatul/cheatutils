package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraTunnelConfig;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinEntityHelper;
import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
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

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
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

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;turn(DD)V", cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo info) {
        if (!MixinMouseHandlerHelper.insideTurnPlayer) {
            return;
        }
        var entity = (Entity) (Object) this;
        if (!(entity instanceof LocalPlayer)) {
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

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onGetEyePosition(float p_20300_, CallbackInfoReturnable<Vec3> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.isActive() && freeCam.shouldOverridePlayerPosition()) {
            var entity = (Entity) (Object) this;
            if (entity instanceof LocalPlayer) {
                info.setReturnValue(new Vec3(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    private void onGetViewVector(float p_20253_, CallbackInfoReturnable<Vec3> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.isActive() && freeCam.shouldOverridePlayerPosition()) {
            var entity = (Entity) (Object) this;
            if (entity instanceof LocalPlayer) {
                info.setReturnValue(this.calculateViewVector(freeCam.getXRot(), freeCam.getYRot()));
                info.cancel();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
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

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V")
    private void onBeforeMoveRelative(float limit, Vec3 input, CallbackInfo info) {
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            MixinEntityHelper.insideMoveRelativeLocalPlayer = true;
            if (ConfigStore.instance.getConfig().movementHackConfig.preserveSpeed) {
                Vec3 original = entity.getDeltaMovement();
                double dx = original.x;
                double dz = original.z;
                double distanceSqr = dx * dx + dz * dz;
                if (distanceSqr > 1e-7) {
                    // rotate existing movement vector
                    double distance = Math.sqrt(distanceSqr);
                    float yRot = entity.getYRot();
                    float f = Mth.sin(yRot * ((float)Math.PI / 180F));
                    float f1 = Mth.cos(yRot * ((float)Math.PI / 180F));
                    Vec3 horizontal = new Vec3(input.x * (double)f1 - input.z * (double)f, 0, input.z * (double)f1 + input.x * (double)f);
                    horizontal = horizontal.normalize().scale(distance);
                    entity.setDeltaMovement(horizontal.x, original.y, horizontal.z);
                }
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/entity/Entity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V")
    private void onAfterMoveRelative(float limit, Vec3 input, CallbackInfo info) {
        var entity = (Entity) (Object) this;
        if (entity instanceof LocalPlayer) {
            MixinEntityHelper.insideMoveRelativeLocalPlayer = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
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
