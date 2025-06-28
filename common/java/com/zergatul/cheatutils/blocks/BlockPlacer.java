package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.concurrent.AfterPlayerAiStepExecutor;
import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.BlockPlacerConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.utils.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.concurrent.CompletableFuture;

public class BlockPlacer {

    private static final Minecraft mc = Minecraft.getInstance();

    public static BlockPlacePlan createPlan(BlockState state, BlockPos pos, BlockPlacingMethod method, BlockPlacerConfig config) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        if (level == null || player == null) {
            return null;
        }

        if (method == BlockPlacingMethod.ITEM_USE) {
            return createItemUsePlan(pos, config);
        }

        BlockState currentState = level.getBlockState(pos);
        if (!currentState.canBeReplaced()) {
            return null;
        }

        CollisionContext collisioncontext = CollisionContext.of(player);
        if (!level.isUnobstructed(state, pos, collisioncontext)) {
            return null;
        }

        if (method != BlockPlacingMethod.AIR_PLACE) {
            for (Direction direction : method.getAllowedDirections()) {
                BlockPos neighbourPos = pos.relative(direction);
                BlockState neighbourState = level.getBlockState(neighbourPos);
                if (!neighbourState.canBeReplaced()) {
                    Vec3 target = method.getTarget(player.getEyePosition(), pos, direction.getOpposite(), false);
                    if (target != null) {
                        Rotation rotation = method.getTargetRotation();
                        if (rotation == null) {
                            return createPlan(pos.getCenter(), neighbourPos, direction.getOpposite(), config);
                        } else {
                            return createPlan(pos.getCenter(), neighbourPos, direction.getOpposite(), rotation, method.isDelayedRotation(), method.getTargetDirection(), config);
                        }
                    }
                }
            }
        }

        if (config.attachToAir) {
            // replaceClicked from BlockPlaceContext
            Vec3 target = method.getTarget(player.getEyePosition(), pos, Direction.UP, true);
            if (target != null) {
                return createPlan(target, pos, Direction.UP, config);
            }
        }

        return null;
    }

    private static BlockPlacePlan createItemUsePlan(BlockPos pos, BlockPlacerConfig config) {
        return createPlan(pos.getCenter(), pos, Direction.UP, config);
    }

    private static BlockPlacePlan createPlan(Vec3 target, BlockPos neighbourPos, Direction direction, BlockPlacerConfig config) {
        if (config.autoRotate) {
            return new BlockPlacePlan() {
                @Override
                public CompletableFuture<Void> apply() {
                    return CompletableFuture.completedFuture(null)
                            .thenRunAsync(() -> FakeRotation.instance.setServerRotation(target), AfterPlayerAiStepExecutor.instance)
                            .thenRunAsync(() -> useItem(target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                }
            };
        } else {
            return new BlockPlacePlan() {
                @Override
                public CompletableFuture<Void> apply() {
                    return CompletableFuture.completedFuture(null)
                            .thenRunAsync(() -> useItem(target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                }
            };
        }
    }

    private static BlockPlacePlan createPlan(Vec3 target, BlockPos neighbourPos, Direction direction, Rotation rotation, boolean delayedRotation, Direction lookDirection, BlockPlacerConfig config) {
        assert mc.player != null;

        if (config.autoRotate) {
            return null; // TODO
        } else {
            if (delayedRotation) {
                return new BlockPlacePlan() {
                    @Override
                    public CompletableFuture<Void> apply() {
                        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                        Rotation closest = Rotation.findClosest(playerRot, rotation, lookDirection);
                        Rotation rotation = closest != null ? closest : playerRot;

                        return CompletableFuture.completedFuture(null)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> {}, TickEndExecutor.instance)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> {}, TickEndExecutor.instance)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> useItem(target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                    }
                };
            } else {
                return new BlockPlacePlan() {
                    @Override
                    public CompletableFuture<Void> apply() {
                        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                        Rotation closest = Rotation.findClosest(playerRot, rotation, lookDirection);

                        return CompletableFuture.completedFuture(null)
                                .thenRunAsync(() -> {
                                    if (closest != null) {
                                        FakeRotation.instance.setServerRotation(closest);
                                    }
                                }, AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> useItem(target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                    }
                };
            }
        }
    }

    private static void useItem(Vec3 target, Direction direction, BlockPos neighbour, BlockPlacerConfig config) {
        assert mc.player != null;
        assert mc.gameMode != null;

        InteractionHand hand = InteractionHand.MAIN_HAND;

        BlockHitResult hit = new BlockHitResult(target, direction, neighbour, false);

        boolean emulateShift = config.useShift && !mc.player.isShiftKeyDown();

        if (emulateShift) {
            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerInputPacket(new Input(
                    mc.player.input.keyPresses.forward(),
                    mc.player.input.keyPresses.backward(),
                    mc.player.input.keyPresses.left(),
                    mc.player.input.keyPresses.right(),
                    mc.player.input.keyPresses.jump(),
                    true,
                    mc.player.input.keyPresses.sprint())));
        }

        InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, hit);
        if (result.consumesAction()) {
            if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                mc.player.swing(hand);
            }
        }

        if (emulateShift) {
            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerInputPacket(new Input(
                    mc.player.input.keyPresses.forward(),
                    mc.player.input.keyPresses.backward(),
                    mc.player.input.keyPresses.left(),
                    mc.player.input.keyPresses.right(),
                    mc.player.input.keyPresses.jump(),
                    false,
                    mc.player.input.keyPresses.sprint())));
        }
    }
}