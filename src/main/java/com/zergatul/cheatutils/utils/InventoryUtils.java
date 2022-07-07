package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventoryUtils {

    public static void moveItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

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
}