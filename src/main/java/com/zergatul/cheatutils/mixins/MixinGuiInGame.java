package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class MixinGuiInGame {

    // HUD rendering happens after the camera entity has been restored.
    @ModifyArg(method = "renderAttackIndicator(FLnet/minecraft/client/gui/ScaledResolution;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 0), index = 0)
    private float modifyDebugCursorPitch(float pitch) {
        return FreeCam.INSTANCE.isActive() ? FreeCam.INSTANCE.getXRot() : pitch;
    }

    @ModifyArg(method = "renderAttackIndicator(FLnet/minecraft/client/gui/ScaledResolution;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 1), index = 0)
    private float modifyDebugCursorYaw(float yaw) {
        return FreeCam.INSTANCE.isActive() ? FreeCam.INSTANCE.getYRot() : yaw;
    }

    @Inject(at = @At("HEAD"), method = "renderAttackIndicator(FLnet/minecraft/client/gui/ScaledResolution;)V", cancellable = true)
    private void onRenderCrosshair(float partialTicks, ScaledResolution resolution, CallbackInfo info) {
        if (!FreeCam.INSTANCE.shouldRenderTarget()) {
            info.cancel();
        }
    }
}