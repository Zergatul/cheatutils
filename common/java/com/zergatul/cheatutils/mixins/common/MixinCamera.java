package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ModifyFieldOfViewEvent;
import com.zergatul.cheatutils.common.events.SimpleCancellableEvent;
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

    @ModifyExpressionValue(method = "calculateFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float onModifyFovModifier(float original) {
        if (Events.ModifyFieldOfViewAnimation.trigger(new SimpleCancellableEvent())) {
            return 1.0f;
        } else {
            return original;
        }
    }
}