package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.concurrent.AfterPlayerAiStepExecutor;
import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.InteractionConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;

import java.util.concurrent.CompletableFuture;

public class BlockBreaker {

    private static final Minecraft mc = Minecraft.getInstance();

    public static BlockBreakPlan createPlan(BlockPos pos, InteractionConfig config) {
        return new BlockBreakPlan() {
            @Override
            public CompletableFuture<Void> apply() {
                CompletableFuture<Void> future = new CompletableFuture<>();
                if (!isInRange(pos, config)) {
                    future.complete(null);
                } else {
                    if (config.shouldAutoRotate()) {
                        FakeRotation.instance.setServerRotation(pos.getCenter());
                    }
                    AfterSendPlayerPosExecutor.instance.execute(() -> {
                        assert mc.player != null;
                        assert mc.gameMode != null;

                        mc.gameMode.startDestroyBlock(pos, Direction.UP);
                        boolean instamined = !mc.gameMode.isDestroying();

                        // if we call continueDestroyBlock after we block is destroyed
                        // it can trigger destroying next block we don't want to touch
                        if (!instamined) {
                            mc.gameMode.continueDestroyBlock(pos, Direction.UP);
                        }
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        if (instamined) {
                            future.complete(null);
                        } else {
                            queueCheckBreakingProgress(pos, config, future);
                        }
                    });
                }
                return future;
            }
        };
    }

    private static void queueCheckBreakingProgress(BlockPos pos, InteractionConfig config, CompletableFuture<Void> future) {
        TickEndExecutor.instance.execute(() -> {
            if (config.shouldAutoRotate()) {
                AfterPlayerAiStepExecutor.instance.execute(() -> {
                    FakeRotation.instance.setServerRotation(pos.getCenter());
                });
            }
            AfterSendPlayerPosExecutor.instance.execute(() -> {
                checkBreakingProgress(pos, config, future);
            });
        });
    }

    private static void checkBreakingProgress(BlockPos pos, InteractionConfig config, CompletableFuture<Void> future) {
        assert mc.player != null;
        assert mc.gameMode != null;

        if (mc.gameMode.isDestroying()) {
            if (mc.options.keyAttack.isDown()) {
                // player is destroying block, cancel
                future.complete(null);
            } else {
                // check distance to block
                if (isInRange(pos, config)) {
                    if (mc.gameMode.continueDestroyBlock(pos, Direction.UP)) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        queueCheckBreakingProgress(pos, config, future);
                    } else {
                        // block destroyed
                        future.complete(null);
                    }
                } else {
                    mc.gameMode.stopDestroyBlock();
                    future.complete(null);
                }
            }
        } else {
            future.complete(null);
        }
    }

    private static boolean isInRange(BlockPos pos, InteractionConfig config) {
        assert mc.player != null;

        return pos.distToCenterSqr(mc.player.getEyePosition()) <= config.getMaxRange() * config.getMaxRange();
    }
}