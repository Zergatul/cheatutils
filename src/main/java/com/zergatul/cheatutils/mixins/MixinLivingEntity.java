package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.FullBrightController;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/LivingEntity;hasEffect(Lnet/minecraft/potion/Effect;)Z", cancellable = true)
    private void onHasEffect(Effect effect, CallbackInfoReturnable<Boolean> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBrightController.instance.insideUpdateLightTexture) {
            if (effect == Effects.NIGHT_VISION) {
                info.setReturnValue(true);
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/LivingEntity;getEffect(Lnet/minecraft/potion/Effect;)Lnet/minecraft/potion/EffectInstance;", cancellable = true)
    private void onGetEffect(Effect effect, CallbackInfoReturnable<EffectInstance> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBrightController.instance.insideUpdateLightTexture) {
            if (effect == Effects.NIGHT_VISION) {
                info.setReturnValue(new EffectInstance(Effects.NIGHT_VISION, 1000));
                info.cancel();
            }
        }
    }
}