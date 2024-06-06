package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.visuals.Zoom;
import com.zergatul.cheatutils.common.events.MouseScrollEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onMouse(DD)V", shift = At.Shift.AFTER))
    private void onLocalPlayerTurn(CallbackInfo info, @Local(index = 11) LocalDoubleRef yRotRef, @Local(index = 13) LocalDoubleRef xRotRef) {
        if (Zoom.instance.isActive()) {
            double xRot = xRotRef.get();
            double yRot = yRotRef.get();
            xRot *= Zoom.instance.getFovFactor();
            yRot *= Zoom.instance.getFovFactor();
            xRotRef.set(xRot);
            yRotRef.set(yRot);
        }
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"),
            method = "onScroll(JDD)V",
            cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        double delta = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * this.minecraft.options.mouseWheelSensitivity().get();
        if (Events.MouseScroll.trigger(new MouseScrollEvent(delta))) {
            info.cancel();
        }
    }
}