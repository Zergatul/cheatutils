package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ScaffoldConfig;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GravelBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScaffoldController {

    public static final ScaffoldController instance = new ScaffoldController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final List<BlockPos> list = new ArrayList<>();

    private ScaffoldController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        ScaffoldConfig config = ConfigStore.instance.getConfig().scaffoldConfig;
        if (config.enabled) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            int yb = MathHelper.floor(y) - 1;

            list.clear();
            list.add(new BlockPos(MathHelper.floor(x), yb, MathHelper.floor(z)));

            if (config.distance > 0) {
                BlockPos bp1 = new BlockPos(new BlockPos(MathHelper.floor(x + config.distance), yb, MathHelper.floor(z)));
                if (!list.contains(bp1)) {
                    list.add(bp1);
                }

                BlockPos bp2 = new BlockPos(new BlockPos(MathHelper.floor(x - config.distance), yb, MathHelper.floor(z)));
                if (!list.contains(bp2)) {
                    list.add(bp2);
                }

                BlockPos bp3 = new BlockPos(new BlockPos(MathHelper.floor(x), yb, MathHelper.floor(z - config.distance)));
                if (!list.contains(bp3)) {
                    list.add(bp3);
                }

                BlockPos bp4 = new BlockPos(new BlockPos(MathHelper.floor(x), yb, MathHelper.floor(z + config.distance)));
                if (!list.contains(bp4)) {
                    list.add(bp4);
                }
            }

            boolean placed = false;
            for (BlockPos bp: list) {
                if (canPlaceBlock(mc.world.getBlockState(bp))) {
                    for (Direction direction: Direction.values()) {
                        BlockPos other = bp.offset(direction);
                        BlockState state = mc.world.getBlockState(other);
                        if (!state.getOutlineShape(mc.world, other).isEmpty()) {
                            placeBlock(bp, direction.getOpposite(), other, config);
                            placed = true;
                            break;
                        }
                    }
                    if (placed) {
                        break;
                    }
                }
            }

            if (!placed && config.attachToAir) {
                for (BlockPos bp: list) {
                    if (canPlaceBlock(mc.world.getBlockState(bp))) {
                        BlockPos other = bp.offset(Direction.DOWN);
                        placeBlock(bp, Direction.UP, other, config);
                        break;
                    }
                }
            }
        }
    }

    private boolean canPlaceBlock(BlockState state) {
        return state.getMaterial().isReplaceable();
    }

    private void placeBlock(BlockPos destination, Direction direction, BlockPos neighbour, ScaffoldConfig config) {
        Optional<Hand> optional = selectItem(config);
        if (optional.isEmpty()) {
            return;
        }

        Hand hand = optional.get();
        ItemStack itemStack = mc.player.getStackInHand(hand);
        Item item = itemStack.getItem();
        //Block block = ((BlockItem) item).getBlock();
        //boolean isSlab = block instanceof SlabBlock;

        Vec3d location = new Vec3d(
                destination.getX() + 0.5f + direction.getOpposite().getOffsetX() * 0.5,
                destination.getY() + 0.5f /*+ (block instanceof SlabBlock ? 0.25f : 0)*/ + direction.getOpposite().getOffsetY() * 0.5,
                destination.getZ() + 0.5f + direction.getOpposite().getOffsetZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(location, direction, neighbour, false);

        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hit);
        if (result.isAccepted()) {
            if (result.shouldSwingHand()) {
                mc.player.swingHand(hand);
            }
        }

        if (itemStack.isEmpty() && config.replaceBlocksFromInventory) {
            for (int i = 9; i < 36; i++) {
                ItemStack itemStack1 = mc.player.getInventory().getStack(i);
                if (!itemStack1.isEmpty() && item == itemStack1.getItem()) {
                    InventoryUtils.moveItemStack(new InventorySlot(i), hand == Hand.MAIN_HAND ? new InventorySlot(mc.player.getInventory().selectedSlot) : new InventorySlot(EquipmentSlot.OFFHAND));
                    return;
                }
            }
        }
    }

    private Optional<Hand> selectItem(ScaffoldConfig config) {
        Item mainHandItem = mc.player.getStackInHand(Hand.MAIN_HAND).getItem();
        if (mainHandItem instanceof BlockItem blockItem && isValidBlock(blockItem.getBlock(), config)) {
            return Optional.of(Hand.MAIN_HAND);
        }

        Item offHandItem = mc.player.getStackInHand(Hand.OFF_HAND).getItem();
        if (offHandItem instanceof BlockItem blockItem && isValidBlock(blockItem.getBlock(), config)) {
            return Optional.of(Hand.OFF_HAND);
        }

        // find item on hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                if (isValidBlock(blockItem.getBlock(), config)) {
                    mc.player.getInventory().selectedSlot = i;
                    return Optional.of(Hand.MAIN_HAND);
                }
            }
        }

        return Optional.empty();
    }

    private boolean isValidBlock(Block block, ScaffoldConfig config) {
        /*if (block instanceof SlabBlock) {
            return config.useSlabs;
        }*/
        if (!block.getDefaultState().isFullCube(mc.world, new BlockPos(0, 0, 0))) {
            return false;
        }
        if (block instanceof GravelBlock) {
            return false;
        }
        return true;
    }
}