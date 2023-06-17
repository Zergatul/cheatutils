package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static PlaceBlockPlan getPlacingPlan(BlockPos pos, boolean attachToAir) {
        return getPlacingPlan(pos, attachToAir, BlockPlacingMethod.ANY);
    }

    public static PlaceBlockPlan getPlacingPlan(BlockPos pos, boolean attachToAir, BlockPlacingMethod method) {
        if (mc.level == null) {
            return null;
        }

        BlockState state = mc.level.getBlockState(pos);
        if (!state.canBeReplaced()) {
            return null;
        }

        if (mc.player != null) {
            CollisionContext collisioncontext = CollisionContext.of(mc.player);
            if (!mc.level.isUnobstructed(Blocks.STONE.defaultBlockState(), pos, collisioncontext)) {
                return null;
            }
        }

        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbourState = mc.level.getBlockState(neighbourPos);
            if (!neighbourState.canBeReplaced()) {
                Vec3 target = method.getTarget(mc.player.getEyePosition(), pos, direction.getOpposite(), false);
                if (target != null) {
                    return new PlaceBlockPlan(pos.immutable(), direction.getOpposite(), neighbourPos, target);
                }
            }
        }

        if (attachToAir) {
            pos = pos.relative(Direction.DOWN);
            Vec3 target = method.getTarget(mc.player.getEyePosition(), pos, Direction.UP, true);
            if (target != null) {
                return new PlaceBlockPlan(pos.immutable(), Direction.DOWN, pos.relative(Direction.UP), target);
            }
        }

        return null;
    }

    public static void applyPlacingPlan(PlaceBlockPlan plan, boolean useShift) {
        placeBlock(plan.destination, plan.direction, plan.neighbour, plan.target, useShift);
    }

    private static void placeBlock(BlockPos destination, Direction direction, BlockPos neighbour, Vec3 target, boolean useShift) {
        if (mc.player == null) {
            return;
        }

        BlockHitResult hit = new BlockHitResult(target, direction, neighbour, false);

        boolean emulateShift = useShift && !mc.player.isShiftKeyDown();

        if (emulateShift) {
            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        }

        InteractionHand hand = InteractionHand.MAIN_HAND;
        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
        if (result.consumesAction()) {
            if (result.shouldSwing()) {
                mc.player.swing(hand);
            }
        }

        if (emulateShift) {
            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    public record PlaceBlockPlan(BlockPos destination, Direction direction, BlockPos neighbour, Vec3 target) {

        public PlaceBlockPlan(BlockPos destination, Direction direction, BlockPos neighbour) {
            this(destination, direction, neighbour, new Vec3(
                    destination.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                    destination.getY() + 0.5f + direction.getOpposite().getStepY() * 0.5,
                    destination.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5));
        }
    }
}