package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.AutoBucketConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.BlockUtils;
import com.zergatul.cheatutils.utils.RotationUtils;
import com.zergatul.cheatutils.utils.VoxelShapeUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.BlockUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoBucketController {

    public static final AutoBucketController instance = new AutoBucketController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoBucketController.class);
    private volatile boolean waterPlaced;
    private volatile long waterPlacedTime;
    private volatile BlockPos apprWaterPlacedPos;
    private volatile BlockPos realWaterPlacedPos;

    private AutoBucketController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.ScannerBlockUpdated.add(this::onBlockUpdated);
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
            if (realWaterPlacedPos != null) {
                // not more than 4 sec
                if (System.nanoTime() - waterPlacedTime < 4000000000L) {
                    Vec3 blockCenter = new Vec3(
                            realWaterPlacedPos.getX() + 0.5,
                            realWaterPlacedPos.getY() + 0.5,
                            realWaterPlacedPos.getZ() + 0.5);
                    double d2 = mc.player.getPosition(1).distanceToSqr(blockCenter);
                    if (d2 < config.reachDistance * config.reachDistance) {
                        // try to pickup water
                        RotationUtils.Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), blockCenter);
                        float oldXRot = mc.player.getXRot();
                        float oldYRot = mc.player.getYRot();
                        mc.player.setXRot(rotation.xRot());
                        mc.player.setYRot(rotation.yRot());
                        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                        mc.player.setXRot(oldXRot);
                        mc.player.setYRot(oldYRot);
                    }
                }

                realWaterPlacedPos = null;
            }
            return;
        }

        // predict next 4 ticks, with 10 steps per tick
        int stepsPerTick = 10;
        int ticks = 4;
        int steps = ticks * stepsPerTick;
        double multiplier = 1d / stepsPerTick;
        Vec3 speedPerStep = speed.multiply(multiplier, multiplier, multiplier);
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
        BlockPos collisionPos = null;
        EntityDimensions dimensions = mc.player.getDimensions(mc.player.getPose());
        stepsLoop:
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
                            collisionPos = pos;
                            break stepsLoop;
                        }
                    }
                }
            }
        }

        if (collisionPos == null) {
            return;
        }

        ItemStack stack = findItemStackToUse(config);
        if (stack == null) {
            return;
        }

        //logger.info("Collision pos = " + collisionPos);

        if (stack.getItem() instanceof BlockItem) {
            BlockUtils.PlaceBlockPlan plan = BlockUtils.getPlacingPlan(collisionPos.above());
            if (plan == null) {
                return;
            }
            double d2 = plan.destination().distToCenterSqr(mc.player.position());
            if (d2 < 16) {
                BlockUtils.applyPlacingPlan(plan, true);
            }
        } else {
            Vec3 lookAt = new Vec3(
                    collisionPos.getX() + 0.5,
                    collisionPos.getY() + 1,
                    collisionPos.getZ() + 0.5);
            RotationUtils.Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), lookAt);
            //logger.info("Rotate x=" + rotation.xRot() + " y=" + rotation.yRot());
            float oldXRot = mc.player.getXRot();
            float oldYRot = mc.player.getYRot();
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());
            HitResult result = mc.player.pick(config.reachDistance, 1, true);
            if (result instanceof BlockHitResult blockHitResult && result.getType() != HitResult.Type.MISS) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                if (config.autoPickUp) {
                    waterPlaced = true;
                    waterPlacedTime = System.nanoTime();
                    apprWaterPlacedPos = blockHitResult.getBlockPos();
                }
            }
            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);
        }
    }

    private void onBlockUpdated(BlockUpdateEvent event) {
        if (waterPlaced) {
            // wait not more than 3 sec
            if (System.nanoTime() - waterPlacedTime > 3000000000L) {
                waterPlaced = false;
                return;
            }

            // check if distance to block update <=3
            if (event.pos().distManhattan(apprWaterPlacedPos) > 3) {
                return;
            }

            if (event.state().is(Blocks.WATER) && event.state().getFluidState().isSource()) {
                waterPlaced = false;
                realWaterPlacedPos = event.pos();
                return;
            }

            if (event.state().hasProperty(BlockStateProperties.WATERLOGGED) && event.state().getValue(BlockStateProperties.WATERLOGGED)) {
                waterPlaced = false;
                realWaterPlacedPos = event.pos();
            }
        }
    }

    private ItemStack findItemStackToUse(AutoBucketConfig config) {
        // search for 0 fall damage items
        // check hotbar
        Inventory inventory = mc.player.getInventory();
        if (config.useWaterBucket) {
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).getItem() == Items.WATER_BUCKET) {
                    inventory.selected = i;
                    return inventory.getItem(i);
                }
            }
        }
        if (config.useSlimeBlock) {
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).getItem() == Items.SLIME_BLOCK) {
                    inventory.selected = i;
                    return inventory.getItem(i);
                }
            }
        }
        if (config.useCobweb) {
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).getItem() == Items.COBWEB) {
                    inventory.selected = i;
                    return inventory.getItem(i);
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
        return false;
    }

    private long toLong(BlockPos pos) {
        return ((long)pos.getX() << 32) | pos.getZ();
    }
}