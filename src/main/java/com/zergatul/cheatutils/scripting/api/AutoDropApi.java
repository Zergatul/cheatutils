package com.zergatul.cheatutils.scripting.api;

public class AutoDropApi {

    /*public void dropItems() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            AutoDropConfig config = ConfigStore.instance.getConfig().autoDropConfig;
            List<InventorySlot> slots = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                ItemStack itemStack = mc.player.getInventory().getStack(i);
                if (!itemStack.isEmpty() && config.items.contains(itemStack.getItem())) {
                    slots.add(new InventorySlot(i));
                }
            }
            InventoryUtils.dropItemStacks(slots);
        }
    }*/
}