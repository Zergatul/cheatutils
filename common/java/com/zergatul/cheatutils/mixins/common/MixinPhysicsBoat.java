package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PhysicsConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(AbstractBoat.class)
public abstract class MixinPhysicsBoat extends Entity {

    @Shadow private boolean inputLeft;
    @Shadow private boolean inputRight;
    @Shadow private boolean inputUp;
    @Shadow private boolean inputDown;
    @Shadow private boolean isAboveBubbleColumn;
    @Shadow private boolean bubbleColumnDirectionIsDown;

    public MixinPhysicsBoat(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "getGroundFriction", at = @At("RETURN"), cancellable = true)
    private void onGetGroundFriction(CallbackInfoReturnable<Float> cir) {
        PhysicsConfig config = ConfigStore.instance.getConfig().physicsConfig;
        if (config.overrideBoatFriction) {
            cir.setReturnValue((float) config.boatFriction);
        }
    }

    @Inject(method = "setInput", at = @At("HEAD"), cancellable = true)
    private void onSetInput(boolean left, boolean right, boolean up, boolean down, CallbackInfo ci) {
        if (!this.level().isClientSide()) return;
        Entity controller = this.getControllingPassenger();
        if (!(controller instanceof Player)) return;

        PhysicsConfig config = ConfigStore.instance.getConfig().physicsConfig;
        if (!config.followLookDirection) return;

        LivingEntity target = getTargetEntity(config, controller);
        if (target == null) return;

        float yaw = target.getYRot();
        float boatYaw = this.getYRot();
        float deltaYaw = Mth.wrapDegrees(yaw - boatYaw);

        this.inputLeft = deltaYaw < -15;
        this.inputRight = deltaYaw > 15;
        this.inputUp = Math.abs(deltaYaw) < 90;
        this.inputDown = false;

        this.isAboveBubbleColumn = true;
        this.bubbleColumnDirectionIsDown = target.getXRot() > 10;

        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        if (!this.level().isClientSide()) return;
        Entity controller = this.getControllingPassenger();
        if (!(controller instanceof Player)) return;

        PhysicsConfig config = ConfigStore.instance.getConfig().physicsConfig;
        if (!config.followLookDirection) return;

        LivingEntity target = getTargetEntity(config, controller);
        if (target == null) return;

        Vec3 movement = this.getDeltaMovement();

        // === VELOCIDAD HORIZONTAL ===
        if (this.inputUp) {
            double currentSpeed = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
            if (currentSpeed > 0.001) {
                double scale = config.lookSpeed / currentSpeed;
                this.setDeltaMovement(movement.x * scale, movement.y, movement.z * scale);
            }
        }

        // === VERTICAL ===
        if (config.verticalMode) {
            float pitch = target.getXRot();
            Vec3 current = this.getDeltaMovement();
            double my;
            if (pitch < -10) my = config.verticalSpeed + 0.15;
            else if (pitch > 10) my = -config.verticalSpeed;
            else my = 0.04;
            this.setDeltaMovement(current.x, my, current.z);
        }
    }

    private LivingEntity getTargetEntity(PhysicsConfig config, Entity controller) {
        if (config.passengerControls) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger != controller && passenger instanceof LivingEntity living) {
                    return living;
                }
            }
        }
        return controller instanceof LivingEntity living ? living : null;
    }
}