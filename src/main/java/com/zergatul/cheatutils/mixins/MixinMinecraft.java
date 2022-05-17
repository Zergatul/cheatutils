package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
    public void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (EntityTracerConfig config: list) {
                if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                    //ModMain.LOGGER.info("onShouldEntityAppearGlowing - {}", entity.getClass().getName());
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }
            }
        }
    }
}