package com.zergatul.cheatutils.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ModifyFieldOfViewEvent;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @ModifyExpressionValue(
            method = "calculateFov",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;modifyFovBasedOnDeathOrFluid(FF)F"))
    private float onModifyCalculatedFieldOfView(float original) {
        ModifyFieldOfViewEvent event = new ModifyFieldOfViewEvent();
        event.fov = original;
        Events.ModifyFieldOfView.trigger(event);
        return event.fov;
    }
}