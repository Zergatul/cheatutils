package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(User.class)
public abstract class MixinUser {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/User;getName()Ljava/lang/String;", cancellable = true)
    private void onGetName(CallbackInfoReturnable<String> info) {
        var config = ConfigStore.instance.getConfig().userNameConfig;
        if (config.enabled && config.name != null && config.name.length() > 0) {
            info.setReturnValue(config.name);
        }
    }
}