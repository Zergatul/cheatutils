package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface CrystalAuraDamageCalculator {
    void begin(Level level, CrystalAuraConfig config, Vec3 center);
    void end();
    BlockGetter getBlockGetter();
    float calculateEndCrystalDamage(Vec3 crystalPosition, LivingEntity entity);
    float calculatePossibleDamage(LivingEntity self, List<LivingEntity> targets, Vec3 crystalPosition);
    float calculatePossibleDamage(LivingEntity self, List<LivingEntity> targets, Vec3 crystalPosition, BlockPos overridePos);
}