package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {

    @Inject(at = @At("HEAD"), method = "isSpectator()Z", cancellable = true)
    private void onIsSpectator(CallbackInfoReturnable<Boolean> info) {
        if (FreeCam.instance.shouldOverrideSpectator((AbstractClientPlayer) (Object) this)) {
            info.setReturnValue(true);
        }
    }
}