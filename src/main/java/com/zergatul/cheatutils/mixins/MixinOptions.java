package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.helpers.MixinOptionsHelper;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class MixinOptions {

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/Options;load()V")
    private void onLoad(CallbackInfo info) {
        MixinOptionsHelper.onOptionsLoad.forEach(Runnable::run);
        MixinOptionsHelper.onOptionsLoad.clear();
    }
}