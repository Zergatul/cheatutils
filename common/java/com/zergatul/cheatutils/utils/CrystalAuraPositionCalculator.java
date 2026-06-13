package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.blocks.SimpleBlockPlacer;
import com.zergatul.cheatutils.blocks.SimpleBlockPlan;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CrystalAuraPositionCalculator {

    public static @Nullable BlockPos calculatePotentialPlacement(
            Level level,
            LivingEntity self,
            Vec3 eyePos,
            List<LivingEntity> targets,
            CrystalAuraDamageCalculator calculator,
            CrystalAuraConfig config
    ) {
        int radius = Mth.ceil(config.placeRange);
        double placeRangeSqr = config.placeRange * config.placeRange;
        BlockGetter levelSlice = calculator.getBlockGetter();

        float bestDamage = 0;
        BlockPos bestCrystalBlockPos = null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            pos.setX(self.blockPosition().getX() + dx);
            for (int dy = -radius; dy <= radius; dy++) {
                pos.setY(self.blockPosition().getY() + dy);
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setZ(self.blockPosition().getZ() + dz);
                    BlockState state = levelSlice.getBlockState(pos);
                    if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    BlockPos crystalBlockPos = pos.above();
                    if (!levelSlice.getBlockState(crystalBlockPos).isAir()) {
                        continue;
                    }

                    if (new AABB(pos).distanceToSqr(eyePos) > placeRangeSqr) {
                        continue;
                    }

                    Vec3 crystalEntityPosition = Vec3.atBottomCenterOf(crystalBlockPos);

                    float damage = calculator.calculatePossibleDamage(self, targets, crystalEntityPosition);
                    if (damage <= bestDamage) {
                        continue;
                    }

                    AABB crystalBB = new AABB(
                            crystalBlockPos.getX(), crystalBlockPos.getY(), crystalBlockPos.getZ(),
                            crystalBlockPos.getX() + 1, crystalBlockPos.getY() + 2, crystalBlockPos.getZ() + 1);

                    // TODO: possible optimization - return first entity
                    List<Entity> collisions = level.getEntities(null, crystalBB);
                    if (!collisions.isEmpty()) {
                        continue;
                    }

                    bestCrystalBlockPos = crystalBlockPos;
                    bestDamage = damage;
                }
            }
        }

        return bestCrystalBlockPos;
    }

    public static @Nullable SupportPlacement calculatePotentialSupportPlacement(
            Level level,
            LocalPlayer player,
            Vec3 eyePos,
            List<LivingEntity> targets,
            CrystalAuraDamageCalculator calculator,
            CrystalAuraConfig config
    ) {
        int radius = Mth.ceil(config.placeRange);
        double placeRangeSqr = config.placeRange * config.placeRange;
        BlockGetter levelSlice = calculator.getBlockGetter();

        float bestDamage = 0;
        BlockPos bestSupportPos = null;
        SimpleBlockPlan bestSupportPlacePlan = null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            pos.setX(player.blockPosition().getX() + dx);
            for (int dy = -radius; dy <= radius; dy++) {
                pos.setY(player.blockPosition().getY() + dy);
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setZ(player.blockPosition().getZ() + dz);
                    BlockState state = levelSlice.getBlockState(pos);
                    if (!state.canBeReplaced()) {
                        continue;
                    }

                    BlockPos crystalBlockPos = pos.above();
                    if (!levelSlice.getBlockState(crystalBlockPos).isAir()) {
                        continue;
                    }

                    if (new AABB(pos).distanceToSqr(eyePos) > placeRangeSqr) {
                        continue;
                    }

                    // bounding box for support block + crystal placement check
                    AABB bb = new AABB(
                            pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);

                    // TODO: possible optimization - return first entity
                    List<Entity> collisions = level.getEntities(null, bb);
                    if (!collisions.isEmpty()) {
                        continue;
                    }

                    SimpleBlockPlan plan = SimpleBlockPlacer.createPlan(
                            levelSlice,
                            pos.immutable(),
                            config.airPlace,
                            config.autoRotate);
                    if (plan == null) {
                        continue;
                    }

                    Vec3 crystalEntityPosition = Vec3.atBottomCenterOf(crystalBlockPos);

                    float damage = calculator.calculatePossibleDamage(player, targets, crystalEntityPosition, pos);
                    if (damage <= bestDamage) {
                        continue;
                    }

                    bestSupportPos = pos.immutable();
                    bestDamage = damage;
                    bestSupportPlacePlan = plan;
                }
            }
        }

        if (bestSupportPos == null) {
            return null;
        }

        return new SupportPlacement(bestSupportPlacePlan, bestSupportPos);
    }

    public record SupportPlacement(SimpleBlockPlan placePlan, BlockPos supportPos) {}
}