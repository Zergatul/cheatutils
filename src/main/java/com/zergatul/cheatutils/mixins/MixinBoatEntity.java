package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BoatEntity.class)
public abstract class MixinBoatEntity {

    @Shadow
    private float velocityDecay;

    @ModifyArg(
            method = "Lnet/minecraft/entity/vehicle/BoatEntity;updateVelocity()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;setVelocity(DDD)V", ordinal = 0),
            index = 0
    )
    private double onFloatBoatSetDeltaMovementX(double dx) {
        var config = ConfigStore.instance.getConfig().boatHackConfig;
        if (config.overrideFriction) {
            return dx / velocityDecay * config.friction;
        } else {
            return dx;
        }
    }

    @ModifyArg(
            method = "Lnet/minecraft/entity/vehicle/BoatEntity;updateVelocity()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;setVelocity(DDD)V", ordinal = 0),
            index = 2
    )
    private double onFloatBoatSetDeltaMovementZ(double dz) {
        var config = ConfigStore.instance.getConfig().boatHackConfig;
        if (config.overrideFriction) {
            return dz / velocityDecay * config.friction;
        } else {
            return dz;
        }
    }
}