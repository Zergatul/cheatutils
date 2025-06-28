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
import org.apache.logging.log4j.LogManager;

import java.util.List;
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
                        List<RotationRange> ranges = method.getRotationRanges();
                        if (ranges == null) {
                            return createPlan(pos.getCenter(), neighbourPos, direction.getOpposite(), config);
                        } else {
                            return createPlan(pos.getCenter(), neighbourPos, direction.getOpposite(), ranges, method.isDelayedRotation(), config);
                        }
                    }
                }
            }
        }

//        if (config.attachToAir) {
//            // replaceClicked from BlockPlaceContext
//            Vec3 target = method.getTarget(player.getEyePosition(), pos, Direction.UP, true);
//            if (target != null) {
//                return new BlockPlacePlan2(config, pos, pos.immutable(), Direction.UP, target, method.getRotation());
//            }
//        }

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
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    FakeRotation.instance.setServerRotation(target);
                    AfterSendPlayerPosExecutor.instance.execute(() -> {
                        useItem(target, direction, neighbourPos, config);
                        future.complete(null);
                    });
                    return future;
                }
            };
        } else {
            return new BlockPlacePlan() {
                @Override
                public CompletableFuture<Void> apply() {
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    AfterSendPlayerPosExecutor.instance.execute(() -> {
                        useItem(target, direction, neighbourPos, config);
                        future.complete(null);
                    });
                    return future;
                }
            };
        }
    }

    private static BlockPlacePlan createPlan(Vec3 target, BlockPos neighbourPos, Direction direction, List<RotationRange> ranges, boolean delayedRotation, BlockPlacerConfig config) {
        assert mc.player != null;

        if (config.autoRotate) {
            return null; // TODO
        } else {
            if (delayedRotation) {
                var logger = LogManager.getLogger(BlockPlacer.class);
                return new BlockPlacePlan() {
                    @Override
                    public CompletableFuture<Void> apply() {
                        //CompletableFuture<Void> future = new CompletableFuture<>();

                        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                        Rotation closest = RotationRange.findClosest(playerRot, ranges);
                        Rotation rotation = closest != null ? closest : playerRot;

                        return CompletableFuture.completedFuture(null)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> {}, TickEndExecutor.instance)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> {}, TickEndExecutor.instance)
                                .thenRunAsync(() -> FakeRotation.instance.setServerRotation(rotation), AfterPlayerAiStepExecutor.instance)
                                .thenRunAsync(() -> useItem(target, direction, neighbourPos, config), AfterSendPlayerPosExecutor.instance);

//                        // rotation for current tick
//                        logger.info("tick={} Setting fake rot={}", mc.player.level().getGameTime(), rotation);
//                        FakeRotation.instance.setServerRotation(rotation);
//                        AfterPlayerAiStepExecutor.instance.execute(() -> {
//                            // rotation for the next tick
//                            logger.info("tick={} Setting fake rot={}", mc.player.level().getGameTime(), rotation);
//                            FakeRotation.instance.setServerRotation(rotation);
//                            AfterPlayerAiStepExecutor.instance.execute(() -> {
//                                // rotation for the next tick
//                                logger.info("tick={} Setting fake rot={}", mc.player.level().getGameTime(), rotation);
//                                FakeRotation.instance.setServerRotation(rotation);
//                                AfterSendPlayerPosExecutor.instance.execute(() -> {
//                                    // use item 2 position sent after
//                                    logger.info("tick={} using item", mc.player.level().getGameTime());
//                                    useItem(target, direction, neighbourPos, config);
//                                    future.complete(null);
//                                });
//                            });
//                        });
//
//                        return future;
                    }
                };
            } else {
                return new BlockPlacePlan() {
                    @Override
                    public CompletableFuture<Void> apply() {
                        CompletableFuture<Void> future = new CompletableFuture<>();

                        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
                        Rotation closest = RotationRange.findClosest(playerRot, ranges);
                        if (closest != null) {
                            FakeRotation.instance.setServerRotation(closest);
                        }

                        AfterSendPlayerPosExecutor.instance.execute(() -> {
                            useItem(target, direction, neighbourPos, config);
                            future.complete(null);
                        });
                        return future;
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

        /*if (rotation != null) {
            // send correct rotation to server
            //NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Rot(rotation.yRot(), rotation.xRot(), mc.player.onGround()));
            float xRot = Float.isNaN(rotation.xRot()) ? mc.player.getXRot() : rotation.xRot();
            float yRot = Float.isNaN(rotation.yRot()) ? mc.player.getYRot() : rotation.yRot();
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(yRot, xRot, mc.player.onGround(), false));

            // server uses yHeadRot, and it happens on the next tick
            // this is temp hack!
            if (!Float.isNaN(rotation.yRot())) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }*/

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