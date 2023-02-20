package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.FullBrightController;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(at = @At("HEAD"), method = "hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", cancellable = true)
    private void onHasEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBrightController.instance.insideUpdateLightTexture) {
            if (effect == StatusEffects.NIGHT_VISION) {
                info.setReturnValue(true);
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Lnet/minecraft/entity/effect/StatusEffectInstance;", cancellable = true)
    private void onGetEffect(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBrightController.instance.insideUpdateLightTexture) {
            if (effect == StatusEffects.NIGHT_VISION) {
                info.setReturnValue(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000));
                info.cancel();
            }
        }
    }
}