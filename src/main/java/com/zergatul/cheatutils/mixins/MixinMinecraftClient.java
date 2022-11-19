package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.controllers.KeyBindingsController;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z", cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        for (EntityTracerConfig config: ConfigStore.instance.getConfig().entities.configs) {
            if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                info.setReturnValue(true);
                info.cancel();
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;close()V")
    private void onClose(CallbackInfo info) {
        ConfigStore.instance.onClose();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;reset()V", shift = At.Shift.AFTER), method = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    private void onDisconnect(Screen screen, CallbackInfo info) {
        ModApiWrapper.triggerOnClientPlayerLoggingOut();
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V")
    private void onHandleInputEvents(CallbackInfo info) {
        KeyBindingsController.instance.onHandleKeyBindings();
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;render(Z)V")
    private void onBeforeRender(boolean tick, CallbackInfo info) {
        ModApiWrapper.triggerOnRenderTickStart();
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;joinWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    private void onBeforeJoinWorld(ClientWorld world, CallbackInfo info) {
        if (world != null) {
            ModApiWrapper.triggerOnWorldUnload();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    private void onBeforeDisconnect(Screen screen, CallbackInfo info) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.world != null) {
            ModApiWrapper.triggerOnWorldUnload();
        }
    }
}