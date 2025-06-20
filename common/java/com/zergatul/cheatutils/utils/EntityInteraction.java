package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.concurrent.AfterPlayerAiStepExecutor;
import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.configs.InteractionConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class EntityInteraction {

    private final static Minecraft mc = Minecraft.getInstance();

    public static EntityInteractionPlan interact(Entity entity, InteractionConfig config) {
        return new EntityInteractionPlan() {
            @Override
            public CompletableFuture<EntityInteractionResult> apply() {
                if (mc.gameMode == null || mc.player == null) {
                    return CompletableFuture.completedFuture(EntityInteractionResult.failed("Player is null"));
                }

                CompletableFuture<EntityInteractionResult> future = CompletableFuture.completedFuture(EntityInteractionResult.success());
                future = stepIfSuccess(
                        future,
                        AfterPlayerAiStepExecutor.instance,
                        result -> {
                            if (!mc.player.canInteractWithEntity(entity, 3.0)) {
                                return EntityInteractionResult.failed("Out of range");
                            } else {
                                if (config.shouldAutoRotate()) {
                                    FakeRotation.instance.setServerRotation(entity.getBoundingBox().getCenter());
                                }
                                return result;
                            }
                        });
                future = stepIfSuccess(
                        future,
                        AfterSendPlayerPosExecutor.instance,
                        result -> {
                            mc.gameMode.interactAt(mc.player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND);
                            mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
                            mc.player.swing(InteractionHand.MAIN_HAND);
                            return result;
                        });

                return future;
            }
        };
    }

    private static CompletableFuture<EntityInteractionResult> stepIfSuccess(
            CompletableFuture<EntityInteractionResult> previous,
            Executor executor,
            Function<EntityInteractionResult, EntityInteractionResult> action
    ) {
        return previous.thenComposeAsync(result -> {
            if (result.isFailed()) {
                return CompletableFuture.completedFuture(result);
            } else {
                return CompletableFuture.completedFuture(result).thenApplyAsync(action, executor);
            }
        });
    }
}