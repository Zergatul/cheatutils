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

    /*@Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/Entity;isCurrentlyGlowing()Z", cancellable = true)
    public void onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigStore.instance.esp) {
            return;
        }
        for (EntityTracerConfig config: ConfigStore.instance.entities) {
            if (config.enabled && config.clazz.isInstance(this)) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
    }*/

    // public int getTeamColor
}
