package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ModifyFieldOfViewEvent;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    private boolean detached;

    @Shadow
    private boolean isPanoramicMode;

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    protected abstract void setRotation(final float yRot, final float xRot);

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    protected abstract void setPosition(final double x, final double y, final double z);

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z", ordinal = 0),
            method = "alignWithEntity",
            cancellable = true)
    private void onAlignWithEntity(float partialTicks, CallbackInfo info) {
        FreeCam freeCam = FreeCam.instance;
        if (freeCam.isActive()) {
            this.detached = true;
            setRotation(freeCam.getYRot(), freeCam.getXRot());
            setPosition(freeCam.getX(), freeCam.getY(), freeCam.getZ());
            info.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "calculateFov",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;modifyFovBasedOnDeathOrFluid(FF)F"))
    private float onModifyCalculatedFieldOfView(float original) {
        ModifyFieldOfViewEvent event = new ModifyFieldOfViewEvent();
        event.fov = original;
        Events.ModifyFieldOfView.trigger(event);
        return event.fov;
    }

    @Inject(at = @At("HEAD"), method = "modifyFovBasedOnDeathOrFluid", cancellable = true)
    private void onModifyFovBasedOnDeathOrFluid(float partialTicks, float fov, CallbackInfoReturnable<Float> info) {
        if (FreeCam.instance.isActive()) {
            info.setReturnValue(fov);
        }
    }
}