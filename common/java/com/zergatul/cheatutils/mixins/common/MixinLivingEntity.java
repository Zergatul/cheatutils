package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.hacks.ElytraFly;
import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(at = @At("HEAD"), method = "hasEffect", cancellable = true)
    private void onHasEffect(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBright.instance.shouldReturnNightVisionEffect) {
            if (effect == MobEffects.NIGHT_VISION) {
                info.setReturnValue(true);
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getEffect", cancellable = true)
    private void onGetEffect(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> info) {
        if (ConfigStore.instance.getConfig().fullBrightConfig.enabled && FullBright.instance.shouldReturnNightVisionEffect) {
            if (effect == MobEffects.NIGHT_VISION) {
                info.setReturnValue(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000));
                info.cancel();
            }
        }
    }
}