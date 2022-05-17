package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;getTeamColor()I", cancellable = true)
    public void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }
        var entity = (Entity) (Object) this;
        var list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            for (EntityTracerConfig config: list) {
                if (config.enabled && config.clazz.isInstance(entity) && config.glow) {
                    info.setReturnValue(config.glowColor.getRGB());
                    info.cancel();
                    return;
                }
            }
        }
    }
}
