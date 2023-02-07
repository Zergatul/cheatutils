package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
    public void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        for (EntityTracerConfig config: ConfigStore.instance.getConfig().entities.configs) {
            if (config.enabled && config.clazz.isInstance(entity) && config.glow && entity.distanceToSqr(player) < config.getGlowMaxDistanceSqr()) {
                info.setReturnValue(true);
                info.cancel();
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Minecraft;close()V")
    private void onClose(CallbackInfo info) {
        ConfigStore.instance.onClose();
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Minecraft;handleKeybinds()V")
    private void onBeforeHandleKeyBindings(CallbackInfo info) {
        ModApiWrapper.BeforeHandleKeyBindings.trigger();
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/Minecraft;handleKeybinds()V")
    private void onAfterHandleKeyBindings(CallbackInfo info) {
        ModApiWrapper.AfterHandleKeyBindings.trigger();
    }

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/Minecraft;createTitle()Ljava/lang/String;", cancellable = true)
    private void onCreateTitle(CallbackInfoReturnable<String> info) {
        if (ConfigStore.instance.getConfig().userNameConfig.showNameInTitle) {
            info.setReturnValue(Minecraft.getInstance().getUser().getName() + " - " + info.getReturnValue());
        }
    }
}