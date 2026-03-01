package com.zergatul.cheatutils.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollisionUtils {

    public static Optional<BlockPos> findSupportBlock(ClientLevel level, AABB box) {
        BlockPos bestPos = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        Vec3 center = box.getBottomCenter();
        for (BlockPos collision : getCollisions(level, box.setMinY(box.minY - 1.0e-6))) {
            double distanceSqr = collision.distToCenterSqr(center);
            if (distanceSqr < bestDistanceSqr || distanceSqr == bestDistanceSqr && (bestPos == null || bestPos.compareTo(collision) < 0)) {
                bestPos = collision;
                bestDistanceSqr = distanceSqr;
            }
        }

        return Optional.ofNullable(bestPos);
    }

    private static List<BlockPos> getCollisions(ClientLevel level, AABB box) {
        Cursor3D cursor = new Cursor3D(
                Mth.floor(box.minX - 1.0E-7) - 1,
                Mth.floor(box.minY - 1.0E-7) - 1,
                Mth.floor(box.minZ - 1.0E-7) - 1,
                Mth.floor(box.maxX + 1.0E-7) + 1,
                Mth.floor(box.maxY + 1.0E-7) + 1,
                Mth.floor(box.maxZ + 1.0E-7) + 1);

        VoxelShape shape = Shapes.create(box);

        List<BlockPos> collisions = new ArrayList<>();
        while (cursor.advance()) {
            int i = cursor.nextX();
            int j = cursor.nextY();
            int k = cursor.nextZ();
            int l = cursor.getNextType();
            if (l == 3) {
                continue;
            }

            BlockGetter blockgetter = getChunk(level, i, k);
            if (blockgetter == null) {
                continue;
            }

            var pos = new BlockPos(i, j, k);
            BlockState blockstate = blockgetter.getBlockState(pos);
            if ((l != 1 || blockstate.hasLargeCollisionShape()) && (l != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
                VoxelShape voxelshape = blockstate.getCollisionShape(level, pos);
                if (voxelshape == Shapes.block()) {
                    if (box.intersects(i, j, k, i + 1.0, j + 1.0, k + 1.0)) {
                        collisions.add(pos);
                    }
                } else {
                    VoxelShape voxelshape1 = voxelshape.move(pos);
                    if (!voxelshape1.isEmpty() && Shapes.joinIsNotEmpty(voxelshape1, shape, BooleanOp.AND)) {
                        collisions.add(pos);
                    }
                }
            }
        }

        return collisions;
    }

    private static BlockGetter getChunk(ClientLevel level, int x, int z) {
        int i = SectionPos.blockToSectionCoord(x);
        int j = SectionPos.blockToSectionCoord(z);
        return level.getChunkForCollisions(i, j);
    }
}