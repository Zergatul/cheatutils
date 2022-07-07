package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ScaffoldConfig;
import com.zergatul.cheatutils.helpers.MixinEntityHelper;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScaffoldController {

    public static final ScaffoldController instance = new ScaffoldController();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<BlockPos> list = new ArrayList<>();

    private ScaffoldController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (mc.player == null || mc.level == null) {
                return;
            }
            ScaffoldConfig config = ConfigStore.instance.getConfig().scaffoldConfig;
            if (config.enabled) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();

                int yb = Mth.floor(y) - 1;

                list.clear();
                list.add(new BlockPos(Mth.floor(x), yb, Mth.floor(z)));

                if (config.distance > 0) {
                    BlockPos bp1 = new BlockPos(new BlockPos(Mth.floor(x + config.distance), yb, Mth.floor(z)));
                    if (!list.contains(bp1)) {
                        list.add(bp1);
                    }

                    BlockPos bp2 = new BlockPos(new BlockPos(Mth.floor(x - config.distance), yb, Mth.floor(z)));
                    if (!list.contains(bp2)) {
                        list.add(bp2);
                    }

                    BlockPos bp3 = new BlockPos(new BlockPos(Mth.floor(x), yb, Mth.floor(z - config.distance)));
                    if (!list.contains(bp3)) {
                        list.add(bp3);
                    }

                    BlockPos bp4 = new BlockPos(new BlockPos(Mth.floor(x), yb, Mth.floor(z + config.distance)));
                    if (!list.contains(bp4)) {
                        list.add(bp4);
                    }
                }

                for (BlockPos bp: list) {
                    if (mc.level.getBlockState(bp).isAir()) {
                        boolean placed = false;
                        for (Direction direction: Direction.values()) {
                            BlockPos other = bp.relative(direction);
                            BlockState state = mc.level.getBlockState(other);
                            if (!state.getShape(mc.level, other).isEmpty()) {
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
            }
        }
    }

    private void placeBlock(BlockPos destination, Direction direction, BlockPos neighbour, ScaffoldConfig config) {
        Vec3 location = new Vec3(
                destination.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                destination.getY() + 0.5f + direction.getOpposite().getStepY() * 0.5,
                destination.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(location, direction, neighbour, false);

        Optional<InteractionHand> optional = selectItem();
        if (optional.isEmpty()) {
            return;
        }

        InteractionHand hand = optional.get();
        ItemStack itemStack = mc.player.getItemInHand(hand);
        Item item = itemStack.getItem();

        InteractionResult result = mc.gameMode.useItemOn(mc.player, mc.level, hand, hit);
        if (result.consumesAction()) {
            if (result.shouldSwing()) {
                mc.player.swing(hand);
            }
        }

        if (itemStack.isEmpty() && config.replaceBlocksFromInventory) {
            for (int i = 9; i < 36; i++) {
                ItemStack itemStack1 = mc.player.getInventory().getItem(i);
                if (!itemStack1.isEmpty() && item == itemStack1.getItem()) {
                    InventoryUtils.moveItemStack(new InventorySlot(i), hand == InteractionHand.MAIN_HAND ? new InventorySlot(mc.player.getInventory().selected) : new InventorySlot(EquipmentSlot.OFFHAND));
                    return;
                }
            }
        }
    }

    private Optional<InteractionHand> selectItem() {
        Item mainHandItem = mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if (mainHandItem instanceof BlockItem blockItem && isValidBlock(blockItem.getBlock())) {
            return Optional.of(InteractionHand.MAIN_HAND);
        }

        Item offHandItem = mc.player.getItemInHand(InteractionHand.OFF_HAND).getItem();
        if (offHandItem instanceof BlockItem blockItem && isValidBlock(blockItem.getBlock())) {
            return Optional.of(InteractionHand.OFF_HAND);
        }

        // find item on hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                if (isValidBlock(blockItem.getBlock())) {
                    mc.player.getInventory().selected = i;
                    return Optional.of(InteractionHand.MAIN_HAND);
                }
            }
        }

        return Optional.empty();
    }

    private boolean isValidBlock(Block block) {
        if (!block.defaultBlockState().isCollisionShapeFullBlock(mc.level, new BlockPos(0, 0, 0))) {
            return false;
        }
        if (block instanceof FallingBlock) {
            return false;
        }
        return true;
    }
}
