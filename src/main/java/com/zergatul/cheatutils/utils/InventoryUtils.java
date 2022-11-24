package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class InventoryUtils {

    public static void moveItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        Inventory inventory = mc.player.getInventory();

        ItemStack air = new ItemStack(Items.AIR, 1);
        ItemStack fromItemStack = fromSlot.get();
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(fromSlot.toServer(), air);
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                fromSlot.toServer(),
                0, // buttonNum
                ClickType.PICKUP,
                fromItemStack,
                int2objectmap
        ));
        fromSlot.set(air);

        ItemStack toItemStack = toSlot.get();
        if (toItemStack.isEmpty()) {
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toServer(), fromItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    toSlot.toServer(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    air,
                    int2objectmap
            ));
            toSlot.set(fromItemStack);
        } else {
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toServer(), fromItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    toSlot.toServer(),
                    0, // buttonNum
                    ClickType.PICKUP,
                    toItemStack,
                    int2objectmap
            ));
            toSlot.set(fromItemStack);

            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(fromSlot.toServer(), toItemStack);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    fromSlot.toServer(),
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

    public static void moveItemStackIntoEmptySlot(int fromSlot, int toSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        Inventory inventory = mc.player.getInventory();

        int serverFromSlot = toServerSlot(fromSlot);
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(serverFromSlot, new ItemStack(Items.AIR, 1));
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                serverFromSlot,
                0, // buttonNum
                ClickType.PICKUP,
                inventory.getItem(serverFromSlot),
                int2objectmap
        ));
        inventory.setItem(fromSlot, int2objectmap.get(serverFromSlot));

        int serverToSlot = toServerSlot(toSlot);
        int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(serverToSlot, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
        NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                0, // containerId
                mc.player.inventoryMenu.getStateId(),
                serverToSlot,
                0, // buttonNum
                ClickType.PICKUP,
                new ItemStack(Items.AIR, 1),
                int2objectmap
        ));
        mc.player.setItemSlot(EquipmentSlot.OFFHAND, int2objectmap.get(45));

        if (!(mc.screen instanceof InventoryScreen)) {
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClosePacket(0));
        }
    }

    public static void dropItemStacks(List<InventorySlot> slots) {
        if (slots == null || slots.size() == 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        for (InventorySlot slot: slots) {
            ItemStack air = new ItemStack(Items.AIR, 1);
            ItemStack fromItemStack = slot.get();
            Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(slot.toServer(), air);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    mc.player.inventoryMenu.getStateId(),
                    slot.toServer(),
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

    private static int toServerSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }
}
