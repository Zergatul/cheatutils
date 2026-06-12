package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class VanillaCrystalAuraDamageCalculator extends AbstractCrystalAuraDamageCalculator {

    private BlockGetter blockGetter;

    @Override
    public void begin(ClientLevel level, CrystalAuraConfig config, Vec3 center) {
        super.begin(level, config, center);
        blockGetter = level;
    }

    @Override
    public void end() {
        super.end();
        blockGetter = null;
    }

    @Override
    public BlockGetter getBlockGetter() {
        return blockGetter;
    }

    @Override
    protected float getSeenPercent(Vec3 center, AABB bb, LivingEntity entity) {
        double xs = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        double ys = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        double zs = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        double xOffset = (1.0 - Math.floor(1.0 / xs) * xs) / 2.0;
        double zOffset = (1.0 - Math.floor(1.0 / zs) * zs) / 2.0;
        if (xs < 0.0 || ys < 0.0 || zs < 0.0) {
            return 0.0F;
        }

        int hits = 0;
        int count = 0;
        for (double xx = 0.0; xx <= 1.0; xx += xs) {
            for (double yy = 0.0; yy <= 1.0; yy += ys) {
                for (double zz = 0.0; zz <= 1.0; zz += zs) {
                    double x = Mth.lerp(xx, bb.minX, bb.maxX);
                    double y = Mth.lerp(yy, bb.minY, bb.maxY);
                    double z = Mth.lerp(zz, bb.minZ, bb.maxZ);
                    Vec3 from = new Vec3(x + xOffset, y, z + zOffset);
                    ClipContext context = new ClipContext(from, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
                    if (blockGetter.clip(context).getType() == HitResult.Type.MISS) {
                        hits++;
                    }

                    count++;
                }
            }
        }

        return (float) hits / count;
    }

    @Override
    protected void pushBlockStateOverride(BlockPos pos, BlockState state) {
        blockGetter = new OverrideBlockGetter(level, pos, state);
    }

    @Override
    protected void popBlockStateOverride() {
        blockGetter = level;
    }

    private static class OverrideBlockGetter implements BlockGetter {

        private final ClientLevel level;
        private final BlockPos overridePos;
        private final BlockState overrideState;

        public OverrideBlockGetter(ClientLevel level, BlockPos overridePos, BlockState overrideState) {
            this.level = level;
            this.overridePos = overridePos;
            this.overrideState = overrideState;
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return level.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.equals(overridePos)) {
                return overrideState;
            }

            return level.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return level.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return level.getHeight();
        }

        @Override
        public int getMinY() {
            return level.getMinY();
        }
    }
}
