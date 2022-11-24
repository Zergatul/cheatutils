package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.MouseScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public abstract class MixinMouse {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"), method = "Lnet/minecraft/client/Mouse;onMouseScroll(JDD)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info, double d) {
        if (ModApiWrapper.MouseScroll.trigger(new MouseScrollEvent(d))) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Mouse;updateMouse()V")
    private void onBeforeTurnPlayer(CallbackInfo info) {
        MixinMouseHandlerHelper.insideTurnPlayer = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/Mouse;updateMouse()V")
    private void onAfterTurnPlayer(CallbackInfo info) {
        MixinMouseHandlerHelper.insideTurnPlayer = false;
    }
}