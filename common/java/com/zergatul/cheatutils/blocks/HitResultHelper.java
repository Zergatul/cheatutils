package com.zergatul.cheatutils.blocks;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HitResultHelper {

    public static BlockHitResult calculate(LocalPlayer player, BlockPos pos, BlockState state) {
        Vec3 from = player.getEyePosition();
        VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, pos, CollisionContext.of(player));
        Vec3 to = getTarget(pos, shape);

        BlockHitResult result = shape.clip(from, to, pos);
        if (result != null) {
            return result;
        }

        return new BlockHitResult(to, getDirection(from, to), pos, false);
    }

    private static Vec3 getTarget(BlockPos pos, VoxelShape shape) {
        if (shape.isEmpty()) {
            return pos.getCenter();
        }

        AABB bounds = shape.bounds();
        return new Vec3(
                pos.getX() + (bounds.minX + bounds.maxX) * 0.5,
                pos.getY() + (bounds.minY + bounds.maxY) * 0.5,
                pos.getZ() + (bounds.minZ + bounds.maxZ) * 0.5);
    }

    private static Direction getDirection(Vec3 from, Vec3 to) {
        Vec3 delta = from.subtract(to);
        if (delta.lengthSqr() < 1.0E-7) {
            return Direction.UP;
        }

        return Direction.getApproximateNearest(delta.x, delta.y, delta.z);
    }
}