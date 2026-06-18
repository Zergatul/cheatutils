package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.extensions.LivingEntityExtension;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements LivingEntityExtension {

    @Shadow
    @Final
    private static EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES;

    public MixinLivingEntity(final EntityType<?> type, final Level level) {
        super(type, level);
        throw new AssertionError();
    }

    public List<ParticleOptions> getParticles_CU() {
        return this.entityData.get(DATA_EFFECT_PARTICLES);
    }
}