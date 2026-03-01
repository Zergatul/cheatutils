package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.CollisionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpeedTelly implements Module {

    public static final SpeedTelly instance = new SpeedTelly();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean enabled;
    private boolean shouldPlace;
    private Vec3 nextLookAt;
    private BlockHitResult placeHitResult;

    private SpeedTelly() {
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.BeforeSendPlayerPos.add(this::onBeforeSendPosition, 10); // should run after FakeRotation
        Events.ClientTickEnd.add(this::onTickEnd);
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    private void onAfterPlayerAiStep() {
        shouldPlace = false;
        nextLookAt = null;
        placeHitResult = null;

        if (!enabled) {
            return;
        }

        if (mc.level == null || mc.player == null) {
            return;
        }

        // check if we are close to fall by creating AABB that's 50% thinner
        EntityDimensions dimensions = mc.player.getDimensions(mc.player.getPose());
        Vec3 speed = mc.player.getDeltaMovement();
        double halfWidth = dimensions.width() * 0.25;
        AABB thinBox = new AABB(
                mc.player.position().x - halfWidth,
                mc.player.position().y,
                mc.player.position().z - halfWidth,
                mc.player.position().x + halfWidth,
                mc.player.position().y + dimensions.height(),
                mc.player.position().z + halfWidth);
        Optional<BlockPos> supportPos = CollisionUtils.findSupportBlock(mc.level, thinBox.move(speed));
        if (supportPos.isPresent()) {
            return;
        }

//        if (mc.level.getGameTime() % tickdelay != 0) {
//            return;
//        }

        BlockPos target = new BlockPos(
                Mth.floor(mc.player.position().x),
                Mth.floor(mc.player.position().y) - 1,
                Mth.floor(mc.player.position().z));

        boolean ok =
                !mc.level.getBlockState(target.below(0)).canBeReplaced() ||
                !mc.level.getBlockState(target.below(1)).canBeReplaced() ||
                !mc.level.getBlockState(target.below(2)).canBeReplaced() ||
                !mc.level.getBlockState(target.below(3)).canBeReplaced();
        if (ok) {
            return;
        }

        Vec3 eyePos = mc.player.getEyePosition();

        double bestDistanceSqr = Double.MAX_VALUE;
        Vec3 bestLookAt = Vec3.ZERO;
        BlockPos bestBlockPos = BlockPos.ZERO;
        List<Direction> directions = new ArrayList<>();
        for (int x = target.getX() - 5; x <= target.getX() + 5; x++) {
            boolean tryEast = x <= target.getX();
            boolean tryWest = x >= target.getX();

            for (int z = target.getZ() - 5; z <= target.getZ() + 5; z++) {
                boolean trySouth = z <= target.getZ();
                boolean tryNorth = z >= target.getZ();

                for (int y = target.getY(); y >= target.getY() - 5; y--) {
                    BlockPos current = new BlockPos(x, y, z);
                    BlockState state = mc.level.getBlockState(current);
                    if (!state.canBeReplaced()) {
                        continue;
                    }

                    directions.clear();
                    if (tryEast) directions.add(Direction.WEST);
                    if (tryWest) directions.add(Direction.EAST);
                    if (trySouth) directions.add(Direction.NORTH);
                    if (tryNorth) directions.add(Direction.SOUTH);
                    directions.add(Direction.DOWN);

                    for (Direction direction : directions) {
                        BlockPos neighborPos = current.relative(direction);
                        BlockState neighborState = mc.level.getBlockState(neighborPos);
                        if (neighborState.isCollisionShapeFullBlock(mc.level, neighborPos)) {
                            Vec3 lookAt = neighborPos.getCenter().add(direction.getOpposite().getUnitVec3().multiply(0.5, 0.5, 0.5));
                            double distanceSqr = eyePos.distanceToSqr(lookAt);
                            if (distanceSqr < bestDistanceSqr) {
                                bestDistanceSqr = distanceSqr;
                                bestLookAt = lookAt;
                                bestBlockPos = current;
                            }
                        }
                    }
                }
            }
        }

        double interactionDistance = mc.player.blockInteractionRange();
        if (bestDistanceSqr > interactionDistance * interactionDistance) {
            return;
        }

        int slot = findItemOnHotbar();
        if (slot < 0) {
            return;
        }

        mc.player.getInventory().setSelectedSlot(slot);

        shouldPlace = true;
        nextLookAt = bestLookAt;
        FakeRotation.instance.setServerRotation(bestLookAt);

        //ModMain.LOGGER.info("Instructed to place block at {}", bestBlockPos);
    }

    private void onBeforeSendPosition() {
        if (!shouldPlace) {
            return;
        }

        if (mc.player == null) {
            shouldPlace = false;
            return;
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            shouldPlace = false;
            return;
        }

        // player rotation is modified by FakeRotation
        HitResult hit = mc.player.raycastHitResult(1, entity);
        if (hit.getType() != HitResult.Type.BLOCK) {
            shouldPlace = false;
            return;
        }

        if (hit.getLocation().distanceToSqr(nextLookAt) > 0.01) {
            shouldPlace = false;
            return;
        }

        placeHitResult = (BlockHitResult) hit;
    }

    private void onTickEnd() {
        if (!shouldPlace) {
            return;
        }

        if (mc.gameMode == null || mc.player == null) {
            return;
        }

        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, placeHitResult);
        //ModMain.LOGGER.info("Tick {} result {}", mc.level.getGameTime(), result.getClass().getSimpleName());

        if (result.consumesAction()) {
            if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    private int findItemOnHotbar() {
        assert mc.player != null;

        if (isFullBlockItem(mc.player.getInventory().getSelectedItem())) {
            return mc.player.getInventory().getSelectedSlot();
        }
        for (int i = 0; i < 9; i++) {
            if (isFullBlockItem(mc.player.getInventory().getItem(i))) {
                return i;
            }
        }

        return -1;
    }

    private boolean isFullBlockItem(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState().isCollisionShapeFullBlock(new EmptyBlockGetter(), BlockPos.ZERO);
        } else {
            return false;
        }
    }

    private static class EmptyBlockGetter implements BlockGetter {

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public int getHeight() {
            return 256;
        }

        @Override
        public int getMinY() {
            return 0;
        }
    }
}