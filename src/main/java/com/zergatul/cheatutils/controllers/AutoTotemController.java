package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoTotemController {

    public static final AutoTotemController instance = new AutoTotemController();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private AutoTotemController() {
        ModApiWrapper.addOnClientTickEnd(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        if (ConfigStore.instance.getConfig().autoTotemConfig.enabled) {
            if (mc.player == null) {
                return;
            }

            ItemStack offhand = mc.player.getStackInHand(Hand.OFF_HAND);
            if (offhand.isEmpty()) {
                Inventory inventory = mc.player.getInventory();
                int totemSlot = -1;
                for (int i = 0; i < 36; i++) {
                    ItemStack itemStack = inventory.getStack(i);
                    if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
                        totemSlot = i;
                        break;
                    }
                }

                if (totemSlot >= 0) {
                    InventoryUtils.moveItemStack(new InventorySlot(totemSlot), new InventorySlot(EquipmentSlot.OFFHAND));
                }
            }
        }
    }
}