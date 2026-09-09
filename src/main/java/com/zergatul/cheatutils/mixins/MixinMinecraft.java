package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onLoadWorld(WorldClient world, String message, CallbackInfo info) {
        Events.LevelUnload.trigger();
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    private void onClose(CallbackInfo info) {
        Events.Close.trigger();
    }
}