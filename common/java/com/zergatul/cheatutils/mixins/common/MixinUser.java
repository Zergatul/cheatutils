package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.modules.UserNameConfig;
import com.zergatul.cheatutils.modules.utilities.UserName;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(User.class)
public abstract class MixinUser {

    @Inject(at = @At("HEAD"), method = "getName", cancellable = true)
    private void onGetName(CallbackInfoReturnable<String> info) {
        UserNameConfig config = UserName.INSTANCE.getSettings().get();
        if (config.enabled() && config.name() != null && !config.name().isEmpty()) {
            info.setReturnValue(config.name());
        }
    }
}