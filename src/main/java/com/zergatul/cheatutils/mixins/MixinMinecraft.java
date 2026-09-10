package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.ui.CustomToast;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Duration;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public abstract GuiToast getToastGui();

    @Inject(method = "init", at = @At("RETURN"))
    private void onInitialized(CallbackInfo info) {
        if (!GLContext.getCapabilities().OpenGL33) {
            this.getToastGui().add(new CustomToast(
                    Duration.ofSeconds(5),
                    new TextComponentString(Constants.MOD_NAME + " ESP requires OpenGL 3.3"),
                    new TextComponentString("Continuing may crash a game")));
        }
    }

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