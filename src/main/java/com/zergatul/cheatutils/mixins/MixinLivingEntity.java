package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.FullBrightController;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", cancellable = true)
    public void onHasEffect(MobEffect effect, CallbackInfoReturnable<Boolean> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBrightController.instance.insideUpdateLightTexture) {
            if (effect == MobEffects.NIGHT_VISION) {
                info.setReturnValue(true);
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/LivingEntity;getEffect(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;", cancellable = true)
    public void onGetEffect(MobEffect effect, CallbackInfoReturnable<MobEffectInstance> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBrightController.instance.insideUpdateLightTexture) {
            if (effect == MobEffects.NIGHT_VISION) {
                info.setReturnValue(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000));
                info.cancel();
            }
        }
    }
}
