package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FastCrystalAuraDamageCalculator extends AbstractCrystalAuraDamageCalculator {

    private int levelSliceRadius;
    private Vec3 levelSliceCenter;
    private LevelSlice levelSlice;

    @Override
    public void begin(Level level, CrystalAuraConfig config, Vec3 center) {
        super.begin(level, config, center);
        this.levelSliceRadius = getLevelSliceRadius(config);
        this.levelSliceCenter = center;
        this.levelSlice = null;
    }

    @Override
    public BlockGetter getBlockGetter() {
        return getLevelSlice();
    }

    public boolean isLineOfSightClear(Vec3 from, Vec3 to) {
        return getLevelSlice().isLineOfSightClear(from, to);
    }

    private LevelSlice getLevelSlice() {
        if (levelSlice == null) {
            levelSlice = LevelSlice.create(level, levelSliceCenter, levelSliceRadius);
        }

        return levelSlice;
    }

    @Override
    public void end() {
        super.end();
        this.levelSlice = null;
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
                    if (isLineOfSightClear(from, center)) {
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
        getBlockGetter();
        levelSlice.pushOverride(pos, state);
    }

    @Override
    protected void popBlockStateOverride() {
        levelSlice.popOverride();
    }

    @NullMarked
    private static class LevelSlice implements BlockGetter {

        private final BlockState[] states;
        private final int xc, yc, zc;
        private final int radius;
        private final int diameter;
        private final int diameterSqr;
        private @Nullable BlockPos overridePos;
        private @Nullable BlockState originalState;

        private LevelSlice(BlockState[] states, int xc, int yc, int zc, int radius, int diameter, int minY, int height) {
            this.states = states;
            this.xc = xc;
            this.yc = yc;
            this.zc = zc;
            this.radius = radius;
            this.diameter = diameter;
            this.diameterSqr = diameter * diameter;
        }

        public static LevelSlice create(BlockGetter level, Vec3 center, int radius) {
            int xc = Mth.floor(center.x);
            int yc = Mth.floor(center.y);
            int zc = Mth.floor(center.z);
            int diameter = radius * 2 + 1;
            int diameterSqr = diameter * diameter;
            BlockState[] states = new BlockState[diameter * diameterSqr];
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
            for (int dx = -radius; dx <= radius; dx++) {
                pos.setX(xc + dx);
                for (int dy = -radius; dy <= radius; dy++) {
                    pos.setY(yc + dy);
                    for (int dz = -radius; dz <= radius; dz++) {
                        pos.setZ(zc + dz);
                        states[(dx + radius) * diameterSqr + (dy + radius) * diameter + dz + radius] = level.getBlockState(pos);
                    }
                }
            }
            return new LevelSlice(states, xc, yc, zc, radius, diameter, level.getMinY(), level.getHeight());
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return states[index(pos)];
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            // we don't need this
            throw new UnsupportedOperationException();
        }

        @Override
        public int getHeight() {
            // we don't need this
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMinY() {
            // we don't need this
            throw new UnsupportedOperationException();
        }

        public void pushOverride(BlockPos pos, BlockState state) {
            overridePos = pos;
            originalState = getBlockState(pos);
            states[index(pos)] = state;
        }

        @SuppressWarnings("ConstantConditions")
        public void popOverride() {
            states[index(overridePos)] = originalState;
            overridePos = null;
            originalState = null;
        }

        private int index(BlockPos pos) {
            return (pos.getX() - xc + radius) * diameterSqr +
                    (pos.getY() - yc + radius) * diameter +
                    pos.getZ() - zc + radius;
        }

        private boolean isLineOfSightClear(Vec3 from, Vec3 to) {
            if (from.equals(to)) {
                return true;
            }

            double toX = Mth.lerp(-1.0E-7, to.x, from.x);
            double toY = Mth.lerp(-1.0E-7, to.y, from.y);
            double toZ = Mth.lerp(-1.0E-7, to.z, from.z);
            double fromX = Mth.lerp(-1.0E-7, from.x, to.x);
            double fromY = Mth.lerp(-1.0E-7, from.y, to.y);
            double fromZ = Mth.lerp(-1.0E-7, from.z, to.z);
            int currentBlockX = Mth.floor(fromX);
            int currentBlockY = Mth.floor(fromY);
            int currentBlockZ = Mth.floor(fromZ);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(currentBlockX, currentBlockY, currentBlockZ);
            if (isRayBlockedByBlock(from, to, pos)) {
                return false;
            }

            double dx = toX - fromX;
            double dy = toY - fromY;
            double dz = toZ - fromZ;
            int signX = Mth.sign(dx);
            int signY = Mth.sign(dy);
            int signZ = Mth.sign(dz);
            double tDeltaX = signX == 0 ? Double.MAX_VALUE : signX / dx;
            double tDeltaY = signY == 0 ? Double.MAX_VALUE : signY / dy;
            double tDeltaZ = signZ == 0 ? Double.MAX_VALUE : signZ / dz;
            double tX = tDeltaX * (signX > 0 ? 1.0 - Mth.frac(fromX) : Mth.frac(fromX));
            double tY = tDeltaY * (signY > 0 ? 1.0 - Mth.frac(fromY) : Mth.frac(fromY));
            double tZ = tDeltaZ * (signZ > 0 ? 1.0 - Mth.frac(fromZ) : Mth.frac(fromZ));

            while (tX <= 1.0 || tY <= 1.0 || tZ <= 1.0) {
                if (tX < tY) {
                    if (tX < tZ) {
                        currentBlockX += signX;
                        tX += tDeltaX;
                    } else {
                        currentBlockZ += signZ;
                        tZ += tDeltaZ;
                    }
                } else if (tY < tZ) {
                    currentBlockY += signY;
                    tY += tDeltaY;
                } else {
                    currentBlockZ += signZ;
                    tZ += tDeltaZ;
                }

                pos.set(currentBlockX, currentBlockY, currentBlockZ);
                if (isRayBlockedByBlock(from, to, pos)) {
                    return false;
                }
            }

            return true;
        }

        private boolean isRayBlockedByBlock(Vec3 from, Vec3 to, BlockPos pos) {
            VoxelShape shape = getBlockState(pos).getCollisionShape(this, pos);
            if (shape.isEmpty()) {
                return false;
            }

            double dx = to.x - from.x;
            double dy = to.y - from.y;
            double dz = to.z - from.z;
            if (dx * dx + dy * dy + dz * dz < 1.0E-7) {
                return false;
            }

            double testX = from.x + dx * 0.001 - pos.getX();
            double testY = from.y + dy * 0.001 - pos.getY();
            double testZ = from.z + dz * 0.001 - pos.getZ();
            List<AABB> boxes = shape.toAabbs();
            for (AABB box : boxes) {
                if (contains(box, testX, testY, testZ) || intersects(box, from, to, pos)) {
                    return true;
                }
            }

            return false;
        }

        private static boolean contains(AABB box, double x, double y, double z) {
            return  x >= box.minX && x < box.maxX &&
                    y >= box.minY && y < box.maxY &&
                    z >= box.minZ && z < box.maxZ;
        }

        private static boolean intersects(AABB box, Vec3 from, Vec3 to, BlockPos pos) {
            double minX = box.minX + pos.getX();
            double minY = box.minY + pos.getY();
            double minZ = box.minZ + pos.getZ();
            double maxX = box.maxX + pos.getX();
            double maxY = box.maxY + pos.getY();
            double maxZ = box.maxZ + pos.getZ();
            double dx = to.x - from.x;
            double dy = to.y - from.y;
            double dz = to.z - from.z;
            if (dx > 1.0E-7 && clipPoint(dx, dy, dz, minX, minY, maxY, minZ, maxZ, from.x, from.y, from.z)) {
                return true;
            }
            if (dx < -1.0E-7 && clipPoint(dx, dy, dz, maxX, minY, maxY, minZ, maxZ, from.x, from.y, from.z)) {
                return true;
            }
            if (dy > 1.0E-7 && clipPoint(dy, dz, dx, minY, minZ, maxZ, minX, maxX, from.y, from.z, from.x)) {
                return true;
            }
            if (dy < -1.0E-7 && clipPoint(dy, dz, dx, maxY, minZ, maxZ, minX, maxX, from.y, from.z, from.x)) {
                return true;
            }
            if (dz > 1.0E-7 && clipPoint(dz, dx, dy, minZ, minX, maxX, minY, maxY, from.z, from.x, from.y)) {
                return true;
            }
            return dz < -1.0E-7 && clipPoint(dz, dx, dy, maxZ, minX, maxX, minY, maxY, from.z, from.x, from.y);
        }

        private static boolean clipPoint(
                double da,
                double db,
                double dc,
                double point,
                double minB,
                double maxB,
                double minC,
                double maxC,
                double fromA,
                double fromB,
                double fromC
        ) {
            double s = (point - fromA) / da;
            double pb = fromB + s * db;
            double pc = fromC + s * dc;
            return 0.0 < s && s < 1.0 &&
                    minB - 1.0E-7 < pb && pb < maxB + 1.0E-7 &&
                    minC - 1.0E-7 < pc && pc < maxC + 1.0E-7;
        }
    }
}