package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Boat.class)
public abstract class MixinBoat {

    @Shadow
    private float invFriction;

    @ModifyArg(
            method = "Lnet/minecraft/world/entity/vehicle/Boat;floatBoat()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(DDD)V", ordinal = 0),
            index = 0
    )
    private double onFloatBoatSetDeltaMovementX(double dx) {
        var config = ConfigStore.instance.getConfig().boatHackConfig;
        if (config.overrideFriction) {
            return dx / invFriction * config.friction;
        } else {
            return dx;
        }
    }

    @ModifyArg(
            method = "Lnet/minecraft/world/entity/vehicle/Boat;floatBoat()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(DDD)V", ordinal = 0),
            index = 2
    )
    private double onFloatBoatSetDeltaMovementZ(double dz) {
        var config = ConfigStore.instance.getConfig().boatHackConfig;
        if (config.overrideFriction) {
            return dz / invFriction * config.friction;
        } else {
            return dz;
        }
    }
}