package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.concurrent.AfterPlayerAiStepExecutor;
import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.controllers.FakeRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

public final class SimpleBlockPlacer {

    // assumes all collision validations are done by calling code
    public static SimpleBlockPlan createPlan(BlockGetter level, BlockPos pos, boolean airPlace, boolean autoRotate) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbourPos);
            if (!neighbourState.canBeReplaced()) {
                return new Plan(Vec3.atCenterOf(pos), neighbourPos, direction.getOpposite(), autoRotate);
            }
        }

        if (airPlace) {
            return new Plan(Vec3.atCenterOf(pos), pos, Direction.UP, autoRotate);
        }

        return null;
    }

    private record Plan(Vec3 target, BlockPos neighbourPos, Direction direction, boolean autoRotate) implements SimpleBlockPlan {

        @Override
        public CompletableFuture<Boolean> apply(InteractionHand hand) {
            if (autoRotate) {
                return CompletableFuture.completedFuture(true)
                        .thenApplyAsync(ignored -> rotate(), AfterPlayerAiStepExecutor.instance)
                        .thenApplyAsync(rotated -> rotated && useItem(hand), AfterSendPlayerPosExecutor.instance);
            } else {
                return CompletableFuture.completedFuture(true)
                        .thenApplyAsync(ignored -> useItem(hand), AfterSendPlayerPosExecutor.instance);
            }
        }

        private boolean rotate() {
            FakeRotation.instance.setServerRotation(target);
            return true;
        }

        private boolean useItem(InteractionHand hand) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.gameMode == null) {
                return false;
            }

            BlockHitResult hit = new BlockHitResult(target, direction, neighbourPos, false);
            InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, hit);
            if (result.consumesAction()) {
                if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                    mc.player.swing(hand);
                }
                return true;
            }

            return false;
        }
    }
}