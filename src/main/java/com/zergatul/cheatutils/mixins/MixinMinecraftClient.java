package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import net.minecraft.client.MinecraftClient;
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
        List<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (EntityTracerConfig config: list) {
                if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                    info.setReturnValue(true);
                    info.cancel();
                    return;
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;close()V")
    private void onClose(CallbackInfo info) {
        ConfigStore.instance.onClose();
    }
}