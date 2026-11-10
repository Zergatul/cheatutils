package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.visuals.Zoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = MouseHandler.class, priority = 10)
public abstract class MixinMouseHandlerZoom {

    @ModifyArg(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), index = 1)
    private double onModifyTurnXRot(double xRot) {
        return Zoom.INSTANCE.isActive() ? xRot * Zoom.INSTANCE.getFovFactor() : xRot;
    }

    @ModifyArg(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), index = 0)
    private double onModifyTurnYRot(double yRot) {
        return Zoom.INSTANCE.isActive() ? yRot * Zoom.INSTANCE.getFovFactor() : yRot;
    }
}