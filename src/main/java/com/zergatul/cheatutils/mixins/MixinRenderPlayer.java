package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @Redirect(
            method = "setModelVisibilities(Lnet/minecraft/client/entity/AbstractClientPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isSpectator()Z"))
    private boolean onSetModelVisibilitiesIsSpectator(AbstractClientPlayer player) {
        FreeCam freeCam = FreeCam.INSTANCE;
        if (freeCam.shouldRenderHands() && freeCam.shouldOverrideSpectator(player)) {
            return false;
        }

        return player.isSpectator();
    }
}