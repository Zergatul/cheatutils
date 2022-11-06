package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockUtils {

    public static boolean placeBlock(Minecraft mc, BlockPos pos) {
        if (mc.level == null) {
            return false;
        }

        BlockState blockState = mc.level.getBlockState(pos);
        if (!blockState.getMaterial().isReplaceable()) {
            return false;
        }

        for (Direction direction: Direction.values()) {
            BlockPos other = pos.relative(direction);
            BlockState state = mc.level.getBlockState(other);
            if (!state.getShape(mc.level, other).isEmpty()) {
                placeBlock(mc, pos, direction.getOpposite(), other);
                return true;
            }
        }

        return false;
    }

    private static void placeBlock(Minecraft mc, BlockPos destination, Direction direction, BlockPos neighbour) {
        Vec3 location = new Vec3(
                destination.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                destination.getY() + 0.5f + direction.getOpposite().getStepY() * 0.5,
                destination.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(location, direction, neighbour, false);

        InteractionHand hand = InteractionHand.MAIN_HAND;
        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
        if (result.consumesAction()) {
            if (result.shouldSwing()) {
                mc.player.swing(hand);
            }
        }
    }
}