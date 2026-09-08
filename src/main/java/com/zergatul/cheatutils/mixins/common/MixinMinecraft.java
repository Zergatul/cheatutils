package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onLoadWorld(WorldClient world, String message, CallbackInfo info) {
        FreeCam.instance.onWorldUnload();
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    private void onClose(CallbackInfo info) {
        Events.Close.trigger();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        LogManager.getLogger(Constants.MOD_ID).info("CheatUtils client initialized; Minecraft mixin is active.");
    }
}
