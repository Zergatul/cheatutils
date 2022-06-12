package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.controllers.FreeCamController;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;", cancellable = true)
    public void onGetTextureLocation(AbstractClientPlayer player, CallbackInfoReturnable<ResourceLocation> info) {
        if (FreeCamController.instance.isActive()) {
            RemotePlayer shadow = FreeCamController.instance.getShadow();
            if (player == shadow) {
                var playerRenderer = (PlayerRenderer) (Object) this;
                info.setReturnValue(playerRenderer.getTextureLocation(FreeCamController.instance.getPlayer()));
                info.cancel();
                return;
            }
        }
    }
}
