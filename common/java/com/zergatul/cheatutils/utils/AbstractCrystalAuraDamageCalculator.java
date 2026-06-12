package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class AbstractCrystalAuraDamageCalculator implements CrystalAuraDamageCalculator {

    protected BlockGetter level;
    private Difficulty difficulty;
    private DamageSource damageSource;
    private double minTargetDamage;
    private double maxSelfDamage;

    @Override
    public void begin(Level level, CrystalAuraConfig config, Vec3 center) {
        this.level = level;
        this.difficulty = level.getDifficulty();
        this.damageSource = level.damageSources().explosion(null);
        this.minTargetDamage = config.minTargetDamage;
        this.maxSelfDamage = config.maxSelfDamage;
    }

    @Override
    public void end() {
        level = null;
        difficulty = null;
        damageSource = null;
    }

    @Override
    public float calculateEndCrystalDamage(Vec3 crystalPosition, LivingEntity entity) {
        float doubleRadius = DamageUtils.END_CRYSTAL_EXPLOSION_RADIUS * 2.0F;
        double distanceModifier = Math.sqrt(entity.distanceToSqr(crystalPosition)) / doubleRadius;
        if (distanceModifier > 1.0) {
            return 0;
        }

        AABB bb = entity.getBoundingBox();
        float exposure = getSeenPercent(crystalPosition, bb, entity);
        double pow = (1.0 - distanceModifier) * exposure;
        float damage = (float) ((pow * pow + pow) / 2.0 * 7.0 * doubleRadius + 1.0);
        return DamageUtils.applyReductions(difficulty, entity, damage, damageSource);
    }

    @Override
    public float calculatePossibleDamage(LocalPlayer self, List<LivingEntity> targets, Vec3 crystalPosition) {
        float targetDamage = 0;
        for (LivingEntity target : targets) {
            targetDamage += calculateEndCrystalDamage(crystalPosition, target);
        }

        if (targetDamage < minTargetDamage) {
            return 0;
        }

        float selfDamage = calculateEndCrystalDamage(crystalPosition, self);
        if (selfDamage > maxSelfDamage) {
            return 0;
        }

        return targetDamage;
    }

    @Override
    public float calculatePossibleDamage(LocalPlayer self, List<LivingEntity> targets, Vec3 crystalPosition, BlockPos overridePos, BlockState overrideState) {
        pushBlockStateOverride(overridePos, overrideState);
        try {
            return calculatePossibleDamage(self, targets, crystalPosition);
        } finally {
            popBlockStateOverride();
        }
    }

    protected abstract float getSeenPercent(Vec3 center, AABB bb, LivingEntity entity);

    protected abstract void pushBlockStateOverride(BlockPos pos, BlockState state);

    protected abstract void popBlockStateOverride();

    protected static int getLevelSliceRadius(CrystalAuraConfig config) {
        return (int) Math.ceil(Math.max(config.placeRange, config.breakRange) + DamageUtils.END_CRYSTAL_EXPLOSION_RADIUS) + 1;
    }
}