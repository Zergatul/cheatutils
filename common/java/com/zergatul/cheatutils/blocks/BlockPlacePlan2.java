package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.configs.BlockPlacerConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.utils.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

public class BlockPlacePlan2 {

    private final boolean useShift;
    private final boolean autoRotate;
    private final BlockPos destination;
    private final BlockPos neighbour;
    private final Direction direction;
    private final Vec3 target;
    private final Rotation rotation;

    public BlockPlacePlan2(BlockPlacerConfig config, BlockPos destination, BlockPos neighbour, Direction direction, Vec3 target) {
        this(config, destination, neighbour, direction, target, null);
    }

    public BlockPlacePlan2(BlockPlacerConfig config, BlockPos destination, BlockPos neighbour, Direction direction, Vec3 target, Rotation rotation) {
        this.useShift = config.useShift;
        this.autoRotate = config.autoRotate;
        this.destination = destination.immutable();
        this.neighbour = neighbour.immutable();
        this.direction = direction;
        this.target = target;
        this.rotation = rotation;
    }

    public CompletableFuture<Void> apply() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (rotation != null) {
            FakeRotation.instance.setServerRotation(rotation.xRot(), rotation.yRot());
        }
        AfterSendPlayerPosExecutor.instance.execute(() -> {
            useItem();
            future.complete(null);
        });
        return future;
    }

    private void useItem() {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        assert mc.gameMode != null;

        InteractionHand hand = InteractionHand.MAIN_HAND;

        BlockHitResult hit = new BlockHitResult(target, direction, neighbour, false);

        boolean emulateShift = useShift && !mc.player.isShiftKeyDown();

//        if (emulateShift) {
//            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
//        }

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

//        if (emulateShift) {
//            NetworkPacketsController.instance.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
//        }
    }
}