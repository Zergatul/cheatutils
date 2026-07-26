package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.concurrent.AfterPlayerAiStepExecutor;
import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.concurrent.InGameTickEndExecutor;
import com.zergatul.cheatutils.configs.BlockPlacerConfig;
import com.zergatul.cheatutils.configs.InteractionConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.mixins.common.accessors.ClientLevelAccessor;
import com.zergatul.cheatutils.utils.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.concurrent.CompletableFuture;

public class BlockPlacer {

    private static final Minecraft mc = Minecraft.getInstance();

    public static BlockPlacePlan createPlan(BlockState state, BlockPos pos, BlockPlacingMethod method, InteractionConfig config) {
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
                            return createPlan(Vec3.atCenterOf(pos), neighbourPos, direction.getOpposite(), config);
                        } else {
                            return createPlan(Vec3.atCenterOf(pos), neighbourPos, direction.getOpposite(), rotation, method.isDelayedRotation(), method.getTargetDirection(), config);
                        }
                    }
                }
            }
        }

        if (shouldAttachToAir(config) && BlockPlacingMethod.canAirPlace(method)) {
            // replaceClicked from BlockPlaceContext
            Vec3 target = method.getTarget(player.getEyePosition(), pos, Direction.UP, true);
            if (target != null) {
                return createPlan(target, pos, Direction.UP, config);
            }
        }

        return null;
    }

    public static BlockPlacePlan createPacketPlan(BlockPos pos, BlockPlacingMethod method) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        if (level == null || player == null) {
            return null;
        }

        if (method == BlockPlacingMethod.ITEM_USE) {
            return null; // TODO?
        }
        if (method == BlockPlacingMethod.AIR_PLACE) {
            return null; // TODO?
        }

        BlockState currentState = level.getBlockState(pos);
        if (!currentState.canBeReplaced()) {
            return null;
        }

        CollisionContext collisioncontext = CollisionContext.of(player);
        if (!level.isUnobstructed(Blocks.STONE.defaultBlockState(), pos, collisioncontext)) {
            return null;
        }

        for (Direction direction : method.getAllowedDirections()) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbourPos);
            if (!neighbourState.canBeReplaced()) {
                Vec3 target = method.getTarget(player.getEyePosition(), pos, direction.getOpposite(), false);
                if (target != null) {
                    Rotation rotation = method.getTargetRotation();
                    if (rotation == null) {
                        return new BlockPlacePlan() {
                            @Override
                            public CompletableFuture<Void> apply(InteractionHand hand) {
                                mc.player.connection.send(new ServerboundUseItemOnPacket(
                                        hand,
                                        new BlockHitResult(Vec3.atCenterOf(pos), direction.getOpposite(), neighbourPos, false),
                                        getSequenceNumber()));
                                return CompletableFuture.completedFuture(null);
                            }
                        };
                    } else {
                        if (method.isDelayedRotation()) {
                            return null; // not possible
                        }
                        return new BlockPlacePlan() {
                            @Override
                            public CompletableFuture<Void> apply(InteractionHand hand) {
                                Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                                Rotation closest = Rotation.findClosest(playerRot, rotation, method.getTargetDirection());

                                mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                                        closest.yRot(),
                                        closest.xRot(),
                                        mc.player.onGround(),
                                        false));
                                mc.player.connection.send(new ServerboundUseItemOnPacket(
                                        hand,
                                        new BlockHitResult(Vec3.atCenterOf(pos), direction.getOpposite(), neighbourPos, false),
                                        getSequenceNumber()));
                                return CompletableFuture.completedFuture(null);
                            }
                        };
                    }
                }
            }
        }

        return null;
    }

    public static BlockPlacingMethod guessMethod(BlockState state) {
        if (state.getBlock() instanceof DirectionalBlock) {
            return BlockPlacingMethod.facing(state.getValue(DirectionalBlock.FACING));
        }
        return BlockPlacingMethod.ANY;
    }

    private static BlockPlacePlan createItemUsePlan(BlockPos pos, InteractionConfig config) {
        return createPlan(Vec3.atCenterOf(pos), pos, Direction.UP, config);
    }

    private static BlockPlacePlan createPlan(Vec3 target, BlockPos neighbourPos, Direction direction, InteractionConfig config) {
        if (config.shouldAutoRotate()) {
            return new BlockPlacePlan() {
                @Override
                public CompletableFuture<Void> apply(InteractionHand hand) {
                    return CompletableFuture.completedFuture(null)
                            .thenRunAsync(() -> FakeRotation.instance.setServerRotation(target), AfterPlayerAiStepExecutor.instance)
                            .thenRunAsync(() -> useItem(hand, target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                }
            };
        } else {
            return new BlockPlacePlan() {
                @Override
                public CompletableFuture<Void> apply(InteractionHand hand) {
                    return CompletableFuture.completedFuture(null)
                            .thenRunAsync(() -> useItem(hand, target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                }
            };
        }
    }

    private static BlockPlacePlan createPlan(Vec3 target, BlockPos neighbourPos, Direction direction, Rotation rotation, boolean delayedRotation, Direction lookDirection, InteractionConfig config) {
        assert mc.player != null;

        if (config.shouldAutoRotate()) {
            return null; // TODO
        } else {
            if (delayedRotation) {
                return new BlockPlacePlan() {
                    @Override
                    public CompletableFuture<Void> apply(InteractionHand hand) {
                        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                        Rotation closest = Rotation.findClosest(playerRot, rotation, lookDirection);
                        Rotation rotation = closest != null ? closest : playerRot;

                        return CompletableFuture.completedFuture(null)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenCompose(unused -> InGameTickEndExecutor.instance.waitTicks(0))
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenCompose(unused -> InGameTickEndExecutor.instance.waitTicks(0))
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> useItem(hand, target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                    }
                };
            } else {
                return new BlockPlacePlan() {
                    @Override
                    public CompletableFuture<Void> apply(InteractionHand hand) {
                        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                        Rotation closest = Rotation.findClosest(playerRot, rotation, lookDirection);

                        return CompletableFuture.completedFuture(null)
                                .thenRunAsync(() -> {
                                    if (closest != null) {
                                        FakeRotation.instance.setServerRotation(closest);
                                    }
                                }, AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> useItem(hand, target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);
                    }
                };
            }
        }
    }

    private static void useItem(InteractionHand hand, Vec3 target, Direction direction, BlockPos neighbour, InteractionConfig config) {
        assert mc.player != null;
        assert mc.gameMode != null;

        BlockHitResult hit = new BlockHitResult(target, direction, neighbour, false);

        boolean emulateShift = shouldUseShift(config) && !mc.player.isShiftKeyDown();

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

    private static int getSequenceNumber() {
        assert mc.level != null;

        BlockStatePredictionHandler handler = ((ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    private static boolean shouldUseShift(InteractionConfig config) {
        if (config instanceof BlockPlacerConfig blockPlacerConfig) {
            return blockPlacerConfig.useShift;
        } else {
            return false;
        }
    }

    private static boolean shouldAttachToAir(InteractionConfig config) {
        if (config instanceof BlockPlacerConfig blockPlacerConfig) {
            return blockPlacerConfig.attachToAir;
        } else {
            return false;
        }
    }
}