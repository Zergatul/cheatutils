package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.visuals.Zoom;
import com.zergatul.cheatutils.common.events.MouseScrollEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(method = "turnPlayer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void onLocalPlayerTurn(LocalPlayer player, double yRot, double xRot) {
        if (Zoom.instance.isActive()) {
            xRot *= Zoom.instance.getFovFactor();
            yRot *= Zoom.instance.getFovFactor();
        }

        FreeCam.instance.onPlayerTurn(player, yRot, xRot);
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