package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoBucketConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.Rotation;
import com.zergatul.cheatutils.utils.RotationUtils;
import com.zergatul.cheatutils.utils.VoxelShapeUtils;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoBucket implements Module {

    public static final AutoBucket instance = new AutoBucket();

    private final Minecraft mc = Minecraft.getInstance();
    private final NoFallMethod[] methods = new NoFallMethod[] {
            new WaterBucketMethod(),
            new PowderSnowMethod(),
            new SlimeBlockMethod(),
            new HoneyBlockMethod(),
            new CobwebBlockMethod(),
            new HayBlockMethod(),
    };
    private NoFallMethod currentMethod;
    private long itemAppliedTime;
    private BlockPos itemAppliedPos;
    private BlockPos itemAppliedRealPos;

    private AutoBucket() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
    }

    private void onClientTickEnd() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        AutoBucketConfig config = ConfigStore.instance.getConfig().autoBucketConfig;
        if (!config.enabled) {
            return;
        }

        if (ConfigStore.instance.getConfig().flyHackConfig.enabled) {
            return;
        }

        if (mc.player.isFallFlying()) {
            // flying with elytra
            return;
        }

        Vec3 speed = mc.player.getDeltaMovement();
        if (speed.y > -config.speedThreshold / 20) {
            tryPickUp(config);
            return;
        }

        BlockPos collisionPos = predictCollisionBlockPos();
        if (collisionPos == null) {
            return;
        }

        NoFallMethod method = chooseMethod(mc.level, config);
        if (method == null) {
            return;
        }

        BlockPos appliedPos = method.tryApply(mc, collisionPos, config);
        if (appliedPos == null) {
            return;
        }

        currentMethod = method;
        itemAppliedTime = System.nanoTime();
        itemAppliedPos = appliedPos;
        itemAppliedRealPos = null;
    }

    private void onBlockUpdated(BlockUpdateEvent event) {
        if (currentMethod != null && itemAppliedRealPos == null) {
            // wait not more than 3 sec
            if (System.nanoTime() - itemAppliedTime > 3000000000L) {
                resetState();
                return;
            }

            // check if distance to block update <=3
            if (event.pos().distManhattan(itemAppliedPos) > 3) {
                return;
            }

            if (currentMethod.blockUpdateMatch(event.state())) {
                itemAppliedRealPos = event.pos();
            }
        }
    }

    private NoFallMethod chooseMethod(ClientLevel level, AutoBucketConfig config) {
        assert mc.player != null;

        Inventory inventory = mc.player.getInventory();
        for (NoFallMethod method : methods) {
            for (int i = 0; i < 9; i++) {
                if (method.isEnabled(config) && method.match(inventory.getItem(i)) && method.checkLevel(level)) {
                    inventory.setSelectedSlot(i);
                    return method;
                }
            }
        }

        return null;
    }

    private boolean isOkToFallOn(BlockState state) {
        if (state.getBlock() == Blocks.SLIME_BLOCK) {
            return true;
        }
        if (state.getBlock() == Blocks.COBWEB) {
            return true;
        }
        if (state.getBlock() == Blocks.WATER) {
            return true;
        }
        if (state.getBlock() == Blocks.POWDER_SNOW) {
            return true;
        }
        return false;
    }

    private BlockPos predictCollisionBlockPos() {
        assert mc.level != null;
        assert mc.player != null;

        // predict next 4 ticks, with 10 steps per tick
        int stepsPerTick = 10;
        int ticks = 4;
        int steps = ticks * stepsPerTick;
        double multiplier = 1d / stepsPerTick;
        Vec3 speedPerStep = mc.player.getDeltaMovement().multiply(multiplier, multiplier, multiplier);
        double hw = mc.player.getBbWidth() / 2;
        double px = mc.player.getX();
        double px1 = px - hw;
        double px2 = px + hw;
        double py = mc.player.getY() - 0.2; // from entity.getOnPosLegacy
        double pz = mc.player.getZ();
        double pz1 = pz - hw;
        double pz2 = pz + hw;
        Set<BlockPos> checked = new HashSet<>();
        List<Long> covered = new ArrayList<>(); // covered XZ by negate fall dmg block
        EntityDimensions dimensions = mc.player.getDimensions(mc.player.getPose());

        for (int i = 0; i < steps; i++) {
            px += speedPerStep.x;
            px1 += speedPerStep.x;
            px2 += speedPerStep.x;
            py += speedPerStep.y;
            pz += speedPerStep.z;
            pz1 += speedPerStep.z;
            pz2 += speedPerStep.z;

            int bx1 = Mth.floor(px1 + 1e-4);
            int bx2 = Mth.floor(px2 - 1e-4);
            int by = Mth.floor(py);
            int bz1 = Mth.floor(pz1 + 1e-4);
            int bz2 = Mth.floor(pz2 - 1e-4);

            BlockPos[] positions = new BlockPos[] {
                    new BlockPos(bx1, by, bz1),
                    new BlockPos(bx1, by, bz2),
                    new BlockPos(bx2, by, bz2),
                    new BlockPos(bx2, by, bz1)
            };
            for (BlockPos pos : positions) {
                if (!checked.contains(pos)) {
                    checked.add(pos);
                    long coveredXZ = toLong(pos);
                    if (covered.contains(coveredXZ)) {
                        continue;
                    }
                    BlockState state = mc.level.getBlockState(pos);
                    if (isOkToFallOn(state)) {
                        covered.add(coveredXZ);
                        continue;
                    }

                    // TODO: check if it already waterlogged?
                    VoxelShape shape = state.getCollisionShape(mc.level, pos);
                    if (!shape.isEmpty()) {
                        if (VoxelShapeUtils.intersects(pos, shape, dimensions.makeBoundingBox(px, py, pz))) {
                            return pos;
                        }
                    }
                }
            }
        }

        return null;
    }

    private void tryPickUp(AutoBucketConfig config) {
        assert mc.player != null;
        assert mc.gameMode != null;

        if (currentMethod == null) {
            return;
        }

        if (!config.autoPickUp) {
            resetState();
            return;
        }

        if (itemAppliedRealPos != null) {
            // not more than 4 sec
            if (System.nanoTime() - itemAppliedTime < 4000000000L) {
                Vec3 blockCenter = Vec3.atCenterOf(itemAppliedRealPos);
                double d2 = mc.player.getPosition(1).distanceToSqr(blockCenter);
                if (d2 < config.reachDistance * config.reachDistance) {
                    if (currentMethod.tryPickUp(mc, blockCenter, config)) {
                        resetState();
                    }
                }
            } else {
                // time out
                resetState();
            }
        }
    }

    private void resetState() {
        currentMethod = null;
        itemAppliedPos = null;
        itemAppliedRealPos = null;
    }

    private long toLong(BlockPos pos) {
        return ((long)pos.getX() << 32) | pos.getZ();
    }

    private interface NoFallMethod {
        boolean isEnabled(AutoBucketConfig config);
        boolean match(ItemStack stack);
        boolean checkLevel(Level level);
        BlockPos tryApply(Minecraft mc, BlockPos collisionPos, AutoBucketConfig config);
        boolean blockUpdateMatch(BlockState state);
        boolean tryPickUp(Minecraft mc, Vec3 blockCenter, AutoBucketConfig config);
    }

    private static class WaterBucketMethod implements NoFallMethod {

        @Override
        public boolean isEnabled(AutoBucketConfig config) {
            return config.useWaterBucket;
        }

        @Override
        public boolean match(ItemStack stack) {
            return stack.is(Items.WATER_BUCKET);
        }

        @Override
        public boolean checkLevel(Level level) {
            return !level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, BlockPos.ZERO);
        }

        @Override
        public BlockPos tryApply(Minecraft mc, BlockPos collisionPos, AutoBucketConfig config) {
            assert mc.player != null;
            assert mc.gameMode != null;

            Vec3 lookAt = new Vec3(
                    collisionPos.getX() + 0.5,
                    collisionPos.getY() + 1,
                    collisionPos.getZ() + 0.5);
            Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), lookAt);

            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());

            HitResult result = mc.player.pick(config.reachDistance, 1, true);
            BlockPos appliedAtPos;
            if (result instanceof BlockHitResult blockHitResult && result.getType() != HitResult.Type.MISS) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                appliedAtPos = blockHitResult.getBlockPos();
            } else {
                appliedAtPos = null;
            }

            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);

            return appliedAtPos;
        }

        @Override
        public boolean blockUpdateMatch(BlockState state) {
            if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
                return true;
            }

            return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);
        }

        @Override
        public boolean tryPickUp(Minecraft mc, Vec3 blockCenter, AutoBucketConfig config) {
            assert mc.player != null;
            assert mc.gameMode != null;

            Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), blockCenter);
            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);

            return true;
        }
    }

    private static class PowderSnowMethod implements NoFallMethod {

        @Override
        public boolean isEnabled(AutoBucketConfig config) {
            return config.usePowderSnowBucket;
        }

        @Override
        public boolean match(ItemStack stack) {
            return stack.is(Items.POWDER_SNOW_BUCKET);
        }

        @Override
        public boolean checkLevel(Level level) {
            return true;
        }

        @Override
        public BlockPos tryApply(Minecraft mc, BlockPos collisionPos, AutoBucketConfig config) {
            assert mc.player != null;
            assert mc.gameMode != null;

            Vec3 lookAt = new Vec3(
                    collisionPos.getX() + 0.5,
                    collisionPos.getY() + 1,
                    collisionPos.getZ() + 0.5);
            Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), lookAt);

            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());

            HitResult result = mc.player.pick(config.reachDistance, 1, true);
            BlockPos appliedAtPos;
            if (result instanceof BlockHitResult blockHitResult && result.getType() != HitResult.Type.MISS) {
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);
                appliedAtPos = blockHitResult.getBlockPos();
            } else {
                appliedAtPos = null;
            }

            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);

            return appliedAtPos;
        }

        @Override
        public boolean blockUpdateMatch(BlockState state) {
            return state.is(Blocks.POWDER_SNOW);
        }

        @Override
        public boolean tryPickUp(Minecraft mc, Vec3 blockCenter, AutoBucketConfig config) {
            assert mc.player != null;
            assert mc.gameMode != null;

            Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), blockCenter);
            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);

            return true;
        }
    }

    private static abstract class AbstractBlockMethod implements NoFallMethod {

        protected abstract BlockItem getItem();

        @Override
        public boolean match(ItemStack stack) {
            return stack.is(getItem());
        }

        @Override
        public boolean checkLevel(Level level) {
            return true;
        }

        @Override
        public BlockPos tryApply(Minecraft mc, BlockPos collisionPos, AutoBucketConfig config) {
            assert mc.player != null;
            assert mc.gameMode != null;

            Vec3 lookAt = new Vec3(
                    collisionPos.getX() + 0.5,
                    collisionPos.getY() + 1,
                    collisionPos.getZ() + 0.5);
            Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), lookAt);

            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());

            HitResult result = mc.player.pick(config.reachDistance, 1, true);
            BlockPos appliedAtPos;
            if (result instanceof BlockHitResult blockHitResult && result.getType() != HitResult.Type.MISS) {
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);
                appliedAtPos = blockHitResult.getBlockPos();
            } else {
                appliedAtPos = null;
            }

            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);

            return appliedAtPos;
        }

        @Override
        public boolean blockUpdateMatch(BlockState state) {
            return state.is(getItem().getBlock());
        }

        @Override
        public boolean tryPickUp(Minecraft mc, Vec3 blockCenter, AutoBucketConfig config) {
            assert mc.player != null;
            assert mc.gameMode != null;

            /*Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), blockCenter);
            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);*/

            return true;
        }
    }

    private static class SlimeBlockMethod extends AbstractBlockMethod {

        @Override
        public boolean isEnabled(AutoBucketConfig config) {
            return config.useSlimeBlock;
        }

        @Override
        protected BlockItem getItem() {
            return (BlockItem) Items.SLIME_BLOCK;
        }
    }

    private static class HoneyBlockMethod extends AbstractBlockMethod {

        @Override
        public boolean isEnabled(AutoBucketConfig config) {
            return config.useHoneyBlock;
        }

        @Override
        protected BlockItem getItem() {
            return (BlockItem) Items.HONEY_BLOCK;
        }
    }

    private static class CobwebBlockMethod extends AbstractBlockMethod {

        @Override
        public boolean isEnabled(AutoBucketConfig config) {
            return config.useCobweb;
        }

        @Override
        protected BlockItem getItem() {
            return (BlockItem) Items.COBWEB;
        }
    }

    private static class HayBlockMethod extends AbstractBlockMethod {

        @Override
        public boolean isEnabled(AutoBucketConfig config) {
            return config.useHayBale;
        }

        @Override
        protected BlockItem getItem() {
            return (BlockItem) Items.HAY_BLOCK;
        }
    }
}