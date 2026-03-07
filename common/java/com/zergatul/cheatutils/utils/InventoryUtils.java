package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
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
        sendPacket(fromSlot, fromItemStack, fromSlot, air);
        fromSlot.set(air);

        ItemStack toItemStack = toSlot.get();
        if (toItemStack.isEmpty()) {
            sendPacket(toSlot, air, toSlot, fromItemStack);
            toSlot.set(fromItemStack);
        } else {
            sendPacket(toSlot, toItemStack, toSlot, fromItemStack);
            toSlot.set(fromItemStack);

            sendPacket(fromSlot, air, fromSlot, toItemStack);
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
        Int2ObjectMap<HashedStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(fromSlot.toMenuIndex(), toHashed(toItemStack));
        int2objectmap.put(toSlot.toMenuIndex(), toHashed(fromItemStack));
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                fromSlot.toMenuIndex(),
                (byte) toSlot.toInventoryIndex(), // buttonNum
                ContainerInput.SWAP,
                int2objectmap,
                toHashed(new ItemStack(Items.AIR, 1))));

        fromSlot.set(toItemStack);
        toSlot.set(fromItemStack);
    }

    public static void dropItemStacks(List<InventorySlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        for (InventorySlot slot: slots) {
            ItemStack air = new ItemStack(Items.AIR, 1);
            ItemStack fromItemStack = slot.get();
            sendPacket(slot, fromItemStack, slot, air);
            slot.set(air);

            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    (short) -999, // slotNum
                    (byte) 0, // buttonNum
                    ContainerInput.PICKUP,
                    new Int2ObjectOpenHashMap<>(),
                    toHashed(air)));
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

        if (!destItemStack.isEmpty() && !ItemStack.isSameItemSameComponents(sourceItemStack, destItemStack)) {
            return;
        }

        // pickup source slot
        ItemStack air = new ItemStack(Items.AIR, 1);
        sendPacket(fromSlot, sourceItemStack, fromSlot, air);
        fromSlot.set(air);

        int total = sourceItemStack.getCount() + destItemStack.getCount();
        if (total <= stackSize) {
            // click on destination
            ItemStack newDestItemStack = destItemStack.copyWithCount(total);
            sendPacket(toSlot, air, toSlot, newDestItemStack);
            toSlot.set(newDestItemStack);
        } else {
            int remainder = total - stackSize;

            // click on destination
            ItemStack newDestItemStack = destItemStack.copyWithCount(stackSize);
            ItemStack remainderItemStack = destItemStack.copyWithCount(remainder);
            sendPacket(toSlot, remainderItemStack, toSlot, newDestItemStack);
            toSlot.set(newDestItemStack);

            // click on source
            sendPacket(toSlot, air, fromSlot, remainderItemStack);
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

    private static void sendPacket(InventorySlot slot, ItemStack stack, InventorySlot changedSlot, ItemStack changedStack) {
        assert mc.player != null;

        Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(changedSlot.toMenuIndex(), toHashed(changedStack));
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                slot.toMenuIndex(), // slotNum
                (byte) 0, // buttonNum
                ContainerInput.PICKUP,
                changedSlots,
                toHashed(stack))); //carriedItem
    }

    private static HashedStack toHashed(ItemStack itemStack) {
        assert mc.player != null;
        return HashedStack.create(itemStack, mc.player.connection.decoratedHashOpsGenenerator());
    }
}