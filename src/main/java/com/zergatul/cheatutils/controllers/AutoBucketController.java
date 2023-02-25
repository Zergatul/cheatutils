package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.AutoBucketConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.ServerboundMovePlayerPacketMixinInterface;
import com.zergatul.cheatutils.utils.BlockUtils;
import com.zergatul.cheatutils.utils.RotationUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class AutoBucketController {

    public static final AutoBucketController instance = new AutoBucketController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(AutoBucketController.class);

    private AutoBucketController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
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
            return;
        }

        // predict next 4 ticks, with 10 steps per tick
        int stepsPerTick = 10;
        int ticks = 4;
        int steps = ticks * stepsPerTick;
        double multiplier = 1d / stepsPerTick;
        Vec3 speedPerStep = speed.multiply(multiplier, multiplier, multiplier);
        double hw = mc.player.getBbWidth() / 2;
        double px1 = mc.player.getX() - hw;
        double px2 = mc.player.getX() + hw;
        double py = mc.player.getY() - 0.2; // from entity.getOnPosLegacy
        double pz1 = mc.player.getZ() - hw;
        double pz2 = mc.player.getZ() + hw;
        Set<BlockPos> checked = new HashSet<>();
        BlockPos collisionPos = null;
        stepsLoop:
        for (int i = 0; i < steps; i++) {
            px1 += speedPerStep.x;
            px2 += speedPerStep.x;
            py += speedPerStep.y;
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
                    BlockState state = mc.level.getBlockState(pos);
                    if (isOkToFallOn(state)) {
                        continue;
                    }
                    // TODO: check shape, check waterlogged state?
                    VoxelShape shape = state.getCollisionShape(mc.level, pos);
                    if (Block.isShapeFullBlock(shape)) {
                        collisionPos = pos;
                        break stepsLoop;
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
            HitResult result = mc.player.pick(mc.gameMode.getPickRange(), 1, true);
            if (result.getType() != HitResult.Type.MISS) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            }
            mc.player.setXRot(oldXRot);
            mc.player.setYRot(oldYRot);
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
        return false;
    }
}