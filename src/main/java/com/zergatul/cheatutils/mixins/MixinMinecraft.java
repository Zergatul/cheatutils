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

    @Inject(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runGameLoop()V", shift = At.Shift.AFTER))
    private void onAfterRunGameCycle(CallbackInfo info) {
        Events.MainLoopFrameEnd.trigger();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onBeforeRunTick(CallbackInfo info) {
        Events.ClientTickStart.trigger();
    }

    @Inject(method = "runTick", at = @At("TAIL"))
    private void onAfterRunTick(CallbackInfo info) {
        Events.ClientTickEnd.trigger();
    }

    @Inject(method = "runTickKeyboard", at = @At("TAIL"))
    private void onAfterRunTickKeyboard(CallbackInfo info) {
        Events.AfterHandleKeyBindings.trigger();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onLoadWorld(WorldClient world, String message, CallbackInfo info) {
        Events.LevelUnload.trigger();
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    private void onClose(CallbackInfo info) {
        Events.Close.trigger();
    }
}