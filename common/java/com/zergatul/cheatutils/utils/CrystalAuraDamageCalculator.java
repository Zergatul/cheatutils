package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface CrystalAuraDamageCalculator {

    void begin(ClientLevel level, CrystalAuraConfig config, Vec3 center);

    void end();

    BlockGetter getBlockGetter();

    float calculateEndCrystalDamage(Vec3 crystalPosition, LivingEntity entity);

    float calculatePossibleDamage(LocalPlayer self, List<LivingEntity> targets, Vec3 crystalPosition);

    float calculatePossibleDamage(LocalPlayer self, List<LivingEntity> targets, Vec3 crystalPosition, BlockPos overridePos, BlockState overrideState);
}