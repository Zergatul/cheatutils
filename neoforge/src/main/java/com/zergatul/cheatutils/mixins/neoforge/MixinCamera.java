package com.zergatul.cheatutils.mixins.neoforge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ModifyFieldOfViewEvent;
import com.zergatul.cheatutils.common.events.SimpleCancellableEvent;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @ModifyExpressionValue(method = "calculateFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;modifyFovBasedOnDeathOrFluid(FFZ)F"))
    private float onModifyCalculatedFieldOfView(float original) {
        ModifyFieldOfViewEvent event = new ModifyFieldOfViewEvent(original);
        Events.ModifyCalculatedFieldOfView.trigger(event);
        return event.fov;
    }

    @Inject(at = @At("HEAD"), method = "modifyFovBasedOnDeathOrFluid(FFZ)F", cancellable = true)
    private void onModifyFovBasedOnDeathOrFluid(float partialTicks, float fov, boolean useFovSetting, CallbackInfoReturnable<Float> info) {
        if (useFovSetting) {
            if (Events.ModifyFieldOfViewBasedOnLiquid.trigger(new SimpleCancellableEvent())) {
                info.setReturnValue(fov);
            }
        }
    }
}