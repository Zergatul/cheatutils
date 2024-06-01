package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.mixin.WrapMethodInsideIfCondition;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MouseHandler.class, priority = 100)
public abstract class MixinMouseHandlerFreeCam {

    @WrapMethodInsideIfCondition(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private static boolean onLocalPlayerTurn(LocalPlayer player, double yRot, double xRot) {
        return FreeCam.instance.onPlayerTurn(yRot, xRot);
    }
}