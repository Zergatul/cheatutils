package com.zergatul.cheatutils.utils;

public class InventoryUtils {

    /*public static void moveItemStack(InventorySlot fromSlot, InventorySlot toSlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
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
    }*/

    /*public static void dropItemStacks(List<InventorySlot> slots) {
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
    }*/
}