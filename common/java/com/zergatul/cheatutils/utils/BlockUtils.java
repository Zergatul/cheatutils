package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.blocks.BlockPlacingMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static PlaceBlockPlan getPlacingPlan(BlockPos pos, boolean attachToAir, BlockPlacingMethod method) {
        return getPlacingPlan(pos, attachToAir, method, Blocks.STONE.defaultBlockState());
    }

    public static PlaceBlockPlan getPlacingPlan(BlockPos pos, boolean attachToAir, BlockPlacingMethod method, BlockState finalState) {
        if (mc.level == null) {
            return null;
        }

        if (method == BlockPlacingMethod.ITEM_USE) {
            return new PlaceBlockPlan(
                    pos.immutable(),
                    Direction.UP,
                    pos.immutable(),
                    pos.getCenter(),
                    null);
        }

        BlockState currentState = mc.level.getBlockState(pos);
        if (!currentState.canBeReplaced()) {
            return null;
        }

        if (mc.player != null) {
            CollisionContext collisioncontext = CollisionContext.of(mc.player);
            if (!mc.level.isUnobstructed(finalState, pos, collisioncontext)) {
                return null;
            }
        }

        if (method != BlockPlacingMethod.AIR_PLACE) {
            for (Direction direction : method.getAllowedDirections()) {
                BlockPos neighbourPos = pos.relative(direction);
                BlockState neighbourState = mc.level.getBlockState(neighbourPos);
                if (!neighbourState.canBeReplaced()) {
                    Vec3 target = method.getTarget(mc.player.getEyePosition(), pos, direction.getOpposite(), false);
                    if (target != null) {
                        return new PlaceBlockPlan(pos.immutable(), direction.getOpposite(), neighbourPos, target, method.getTargetRotation());
                    }
                }
            }
        }

        if (attachToAir) {
            // replaceClicked from BlockPlaceContext
            Vec3 target = method.getTarget(mc.player.getEyePosition(), pos, Direction.UP, true);
            if (target != null) {
                return new PlaceBlockPlan(pos.immutable(), Direction.UP, pos.immutable(), target, method.getTargetRotation());
            }
        }

        return null;
    }

    public record PlaceBlockPlan(BlockPos destination, Direction direction, BlockPos neighbour, Vec3 target, Rotation rotation) {}
}