package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        LogManager.getLogger(Constants.MOD_ID).info("CheatUtils client initialized; Minecraft mixin is active.");
    }
}
