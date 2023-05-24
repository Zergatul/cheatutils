package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class InventoryUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void moveItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        if (mc.player == null) {
            return;
        }

        ItemStack air = new ItemStack(Items.AIR, 1);
        ItemStack fromItemStack = fromSlot.get();
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(fromSlot.toMenuIndex(), air);
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                fromSlot.toMenuIndex(),
                0, // buttonNum
                ClickType.PICKUP,
                fromItemStack,
                int2objectmap
        ));
        fromSlot.set(air);

        ItemStack toItemStack = toSlot.get();
        if (toItemStack.isEmpty()) {
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toMenuIndex(), fromItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    toSlot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    air,
                    int2objectmap
            ));
            toSlot.set(fromItemStack);
        } else {
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toMenuIndex(), fromItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    toSlot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    toItemStack,
                    int2objectmap
            ));
            toSlot.set(fromItemStack);

            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(fromSlot.toMenuIndex(), toItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    fromSlot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    air,
                    int2objectmap
            ));
            fromSlot.set(toItemStack);
        }

        if (!(mc.screen instanceof InventoryScreen)) {
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClosePacket(0));
        }
    }

    public static void swapItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        if (mc.player == null) {
            return;
        }

        ItemStack fromItemStack = fromSlot.get();
        ItemStack toItemStack = toSlot.get();
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(fromSlot.toMenuIndex(), toItemStack);
        int2objectmap.put(toSlot.toMenuIndex(), fromItemStack);
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                fromSlot.toMenuIndex(),
                toSlot.toInventoryIndex(), // buttonNum
                ClickType.SWAP,
                new ItemStack(Items.AIR, 1),
                int2objectmap));

        fromSlot.set(toItemStack);
        toSlot.set(fromItemStack);
    }

    public static void dropItemStacks(List<InventorySlot> slots) {
        if (slots == null || slots.size() == 0) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        for (InventorySlot slot: slots) {
            ItemStack air = new ItemStack(Items.AIR, 1);
            ItemStack fromItemStack = slot.get();
            Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(slot.toMenuIndex(), air);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    slot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    fromItemStack,
                    int2objectmap
            ));
            slot.set(air);

            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    -999, // slotNum
                    0, // buttonNum
                    ClickType.PICKUP,
                    air,
                    new Int2ObjectOpenHashMap<>()
            ));
        }

        if (!(mc.screen instanceof InventoryScreen)) {
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClosePacket(0));
        }
    }

    public static void addItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        ItemStack sourceItemStack = fromSlot.get();
        ItemStack destItemStack = toSlot.get();

        int stackSize = destItemStack.getMaxStackSize();
        if (destItemStack.getCount() >= stackSize) {
            return;
        }

        if (!destItemStack.isEmpty() && !ItemStack.isSameItemSameTags(sourceItemStack, destItemStack)) {
            return;
        }

        // pickup source slot
        ItemStack air = new ItemStack(Items.AIR, 1);
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(fromSlot.toMenuIndex(), air);
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                fromSlot.toMenuIndex(),
                0, // buttonNum
                ClickType.PICKUP,
                sourceItemStack,
                int2objectmap
        ));
        fromSlot.set(air);

        int total = sourceItemStack.getCount() + destItemStack.getCount();
        if (total <= stackSize) {
            // click on destination
            ItemStack newDestItemStack = destItemStack.copyWithCount(total);
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toMenuIndex(), newDestItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    toSlot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    air,
                    int2objectmap
            ));
            toSlot.set(newDestItemStack);
        } else {
            int remainder = total - stackSize;

            // click on destination
            ItemStack newDestItemStack = destItemStack.copyWithCount(stackSize);
            ItemStack remainderItemStack = destItemStack.copyWithCount(remainder);
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toMenuIndex(), newDestItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    toSlot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    remainderItemStack,
                    int2objectmap
            ));
            toSlot.set(newDestItemStack);

            // click on source
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(fromSlot.toMenuIndex(), remainderItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    fromSlot.toMenuIndex(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    air,
                    int2objectmap
            ));
            fromSlot.set(remainderItemStack);
        }
    }

    public static boolean hasItem(Item item) {
        if (mc.player == null) {
            return false;
        }

        Inventory inventory = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i).is(item)) {
                return true;
            }
        }

        return false;
    }

    public static boolean selectItem(Item item, int slot) {
        if (mc.player == null) {
            return false;
        }

        Inventory inventory = mc.player.getInventory();
        if (inventory.getItem(slot).is(item)) {
            return true;
        }

        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i).is(item)) {
                moveItemStack(new InventorySlot(i), new InventorySlot(slot));
                return true;
            }
        }

        return false;
    }
}