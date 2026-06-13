package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.concurrent.AfterPlayerAiStepExecutor;
import com.zergatul.cheatutils.concurrent.AfterSendPlayerPosExecutor;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.utils.HotbarSlot;
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
        public CompletableFuture<Boolean> apply(HotbarSlot slot, boolean silentSwitch) {
            if (autoRotate) {
                return CompletableFuture.completedFuture(true)
                        .thenApplyAsync(ignored -> rotate(), AfterPlayerAiStepExecutor.instance)
                        .thenApplyAsync(rotated -> rotated && useItem(slot, silentSwitch), AfterSendPlayerPosExecutor.instance);
            } else {
                return CompletableFuture.completedFuture(true)
                        .thenApplyAsync(ignored -> useItem(slot, silentSwitch), AfterSendPlayerPosExecutor.instance);
            }
        }

        private boolean rotate() {
            FakeRotation.instance.setServerRotation(target);
            return true;
        }

        private boolean useItem(HotbarSlot slot, boolean silentSwitch) {
            if (silentSwitch) {
                return useItemSilently(slot);
            } else {
                return useItemNormally(slot);
            }
        }

        private boolean useItemNormally(HotbarSlot slot) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.gameMode == null) {
                return false;
            }

            if (slot.getHand() == InteractionHand.MAIN_HAND) {
                mc.player.getInventory().setSelectedSlot(slot.getSlot());
            }

            BlockHitResult hit = new BlockHitResult(target, direction, neighbourPos, false);
            InteractionResult result = mc.gameMode.useItemOn(mc.player, slot.getHand(), hit);
            if (result.consumesAction()) {
                if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                    mc.player.swing(slot.getHand());
                }
                return true;
            }

            return false;
        }

        private boolean useItemSilently(HotbarSlot slot) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.gameMode == null) {
                return false;
            }

            int uiSlot = -1;
            if (slot.getHand() == InteractionHand.MAIN_HAND && slot.getSlot() != mc.player.getInventory().getSelectedSlot()) {
                uiSlot = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(slot.getSlot());
            }

            BlockHitResult hit = new BlockHitResult(target, direction, neighbourPos, false);
            InteractionResult result = mc.gameMode.useItemOn(mc.player, slot.getHand(), hit);

            if (uiSlot >= 0) {
                mc.player.getInventory().setSelectedSlot(uiSlot);
            }

            if (result.consumesAction()) {
                if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                    mc.player.swing(slot.getHand());
                }
                return true;
            }

            return false;
        }
    }
}