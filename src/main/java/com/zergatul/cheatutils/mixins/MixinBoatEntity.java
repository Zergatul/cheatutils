package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.BoatHackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class MixinBoatEntity extends Entity {

    @Shadow
    private float velocityDecay;

    @Shadow
    private boolean pressingForward;

    @Shadow
    private boolean pressingLeft;

    @Shadow
    private boolean pressingRight;

    @Shadow
    private boolean pressingBack;

    public MixinBoatEntity(EntityType<?> type, World world) {
        super(type, world);
    }

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

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/vehicle/BoatEntity;updatePaddles()V")
    private void onControlBoat(CallbackInfo info) {
        if (!world.isClient()) {
            return;
        }

        BoatHackConfig config = ConfigStore.instance.getConfig().boatHackConfig;
        if (config.fly) {
            this.pressingForward = false;
            this.pressingLeft = false;
            this.pressingRight = false;
            this.pressingBack = false;
        }
    }

    @Override
    public void move(MovementType type, Vec3d speed) {
        if (!world.isClient()) {
            super.move(type, speed);
            return;
        }

        BoatHackConfig config = ConfigStore.instance.getConfig().boatHackConfig;
        if (!config.fly) {
            super.move(type, speed);
            return;
        }

        BoatEntity boat = (BoatEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (boat.getControllingPassenger() != player) {
            super.move(type, speed);
            return;
        }

        boat.setYaw(player.getYaw());

        double angle = boat.getYaw() * Math.PI / 180;
        speed = Vec3d.ZERO;

        boolean up = mc.options.forwardKey.isPressed();
        boolean down = mc.options.backKey.isPressed();
        boolean left = mc.options.leftKey.isPressed();
        boolean right = mc.options.rightKey.isPressed();
        if (up && down) {
            up = false;
            down = false;
        }
        if (left && right) {
            left = false;
            right = false;
        }
        if (up || down || left || right) {
            if (up) {
                if (left) {
                    angle -= Math.PI / 4;
                } else if (right) {
                    angle += Math.PI / 4;
                }
            } else if (down) {
                if (left) {
                    angle -= 3 * Math.PI / 4;
                } else if (right) {
                    angle += 3 * Math.PI / 4;
                } else {
                    angle += Math.PI;
                }
            } else if (left) {
                angle -= Math.PI / 2;
            } else {
                angle += Math.PI / 2;
            }
            speed = new Vec3d(-config.horizontalFlySpeed / 20 * Math.sin(angle), 0, config.horizontalFlySpeed / 20 * Math.cos(angle));
        }

        if (mc.options.jumpKey.isPressed()) {
            speed = speed.add(0, config.verticalFlySpeed / 20, 0);
        }
        if (mc.options.sprintKey.isPressed()) {
            speed = speed.add(0, -config.verticalFlySpeed / 20, 0);
        }

        this.setVelocity(speed);

        super.move(type, speed);
    }
}