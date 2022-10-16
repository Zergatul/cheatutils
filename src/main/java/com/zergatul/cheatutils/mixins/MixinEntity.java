package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow(aliases = "Lnet/minecraft/world/entity/Entity;calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;")
    protected abstract Vector3d calculateViewVector(float p_20172_, float p_20173_);

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        Entity entity = (Entity) (Object) this;
        List<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (EntityTracerConfig config: list) {
                if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                    info.setReturnValue(config.glowColor.getRGB());
                    info.cancel();
                    return;
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;turn(DD)V", cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo info) {
        if (!MixinMouseHandlerHelper.insideTurnPlayer) {
            return;
        }
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof ClientPlayerEntity)) {
            return;
        }
        if (FreeCamController.instance.isActive()) {
            FreeCamController.instance.onMouseTurn(yRot, xRot);
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getEyePosition(F)Lnet/minecraft/util/math/vector/Vector3d;", cancellable = true)
    private void onGetEyePosition(float p_20300_, CallbackInfoReturnable<Vector3d> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.isActive() && freeCam.shouldOverridePlayerPosition()) {
            Entity entity = (Entity) (Object) this;
            if (entity instanceof ClientPlayerEntity) {
                info.setReturnValue(new Vector3d(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;getViewVector(F)Lnet/minecraft/util/math/vector/Vector3d;", cancellable = true)
    private void onGetViewVector(float p_20253_, CallbackInfoReturnable<Vector3d> info) {
        FreeCamController freeCam = FreeCamController.instance;
        if (freeCam.isActive() && freeCam.shouldOverridePlayerPosition()) {
            Entity entity = (Entity) (Object) this;
            if (entity instanceof ClientPlayerEntity) {
                info.setReturnValue(this.calculateViewVector(freeCam.getXRot(), freeCam.getYRot()));
                info.cancel();
            }
        }
    }
}