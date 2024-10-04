package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.LivingEntityExtension;
import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements LivingEntityExtension {

    @Shadow
    @Final
    private static EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;

    public MixinLivingEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        throw new AssertionError();
    }

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

    public List<ParticleOptions> getParticles_CU() {
        return this.entityData.get(DATA_EFFECT_PARTICLES);
    }
}