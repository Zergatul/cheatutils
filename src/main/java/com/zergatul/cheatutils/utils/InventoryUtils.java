package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class InventoryUtils {

    public static void moveItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }

        ItemStack air = new ItemStack(Items.AIR, 1);
        ItemStack fromItemStack = fromSlot.get();
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
        int2objectmap.put(fromSlot.toServer(), air);
        NetworkPacketsController.instance.sendPacket(new ClickSlotC2SPacket(
                0, // containerId
                mc.player.playerScreenHandler.getRevision(),
                fromSlot.toServer(),
                0, // buttonNum
                SlotActionType.PICKUP,
                fromItemStack,
                int2objectmap
        ));
        fromSlot.set(air);

        ItemStack toItemStack = toSlot.get();
        if (toItemStack.isEmpty()) {
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toServer(), fromItemStack);
            NetworkPacketsController.instance.sendPacket(new ClickSlotC2SPacket(
                    0, // containerId
                    mc.player.playerScreenHandler.getRevision(),
                    toSlot.toServer(),
                    0, // buttonNum
                    SlotActionType.PICKUP,
                    air,
                    int2objectmap
            ));
            toSlot.set(fromItemStack);
        } else {
            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(toSlot.toServer(), fromItemStack);
            NetworkPacketsController.instance.sendPacket(new ClickSlotC2SPacket(
                    0, // containerId
                    mc.player.playerScreenHandler.getRevision(),
                    toSlot.toServer(),
                    0, // buttonNum
                    SlotActionType.PICKUP,
                    toItemStack,
                    int2objectmap
            ));
            toSlot.set(fromItemStack);

            int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(fromSlot.toServer(), toItemStack);
            NetworkPacketsController.instance.sendPacket(new ClickSlotC2SPacket(
                    0, // containerId
                    mc.player.playerScreenHandler.getRevision(),
                    fromSlot.toServer(),
                    0, // buttonNum
                    SlotActionType.PICKUP,
                    air,
                    int2objectmap
            ));
            fromSlot.set(toItemStack);
        }

        if (!(mc.currentScreen instanceof InventoryScreen)) {
            NetworkPacketsController.instance.sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }

    public static void dropItemStacks(List<InventorySlot> slots) {
        if (slots == null || slots.size() == 0) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }

        for (InventorySlot slot: slots) {
            ItemStack air = new ItemStack(Items.AIR, 1);
            ItemStack fromItemStack = slot.get();
            Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(slot.toServer(), air);
            NetworkPacketsController.instance.sendPacket(new ClickSlotC2SPacket(
                    0, // containerId
                    mc.player.playerScreenHandler.getRevision(),
                    slot.toServer(),
                    0, // buttonNum
                    SlotActionType.PICKUP,
                    fromItemStack,
                    int2objectmap
            ));
            slot.set(air);

            NetworkPacketsController.instance.sendPacket(new ClickSlotC2SPacket(
                    0, // containerId
                    mc.player.playerScreenHandler.getRevision(),
                    -999, // slotNum
                    0, // buttonNum
                    SlotActionType.PICKUP,
                    air,
                    new Int2ObjectOpenHashMap<>()
            ));
        }

        if (!(mc.currentScreen instanceof InventoryScreen)) {
            NetworkPacketsController.instance.sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }
}