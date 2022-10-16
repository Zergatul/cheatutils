package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinMouseHandlerHelper;
import net.minecraft.client.MouseHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public abstract class MixinMouseHelper {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MouseHelper;turnPlayer()V")
    private void onBeforeTurnPlayer(CallbackInfo info) {
        MixinMouseHandlerHelper.insideTurnPlayer = true;
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/MouseHelper;turnPlayer()V")
    private void onAfterTurnPlayer(CallbackInfo info) {
        MixinMouseHandlerHelper.insideTurnPlayer = false;
    }
}
