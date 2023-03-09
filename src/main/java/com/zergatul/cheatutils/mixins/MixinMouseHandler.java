package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.FreeCamController;
import com.zergatul.cheatutils.controllers.ZoomController;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Redirect(method = "turnPlayer()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void onLocalPlayerTurn(LocalPlayer player, double yRot, double xRot) {
        if (ZoomController.instance.isActive()) {
            xRot *= ZoomController.instance.getFovFactor();
            yRot *= ZoomController.instance.getFovFactor();
        }

        FreeCamController.instance.onPlayerTurn(player, yRot, xRot);
    }
}
