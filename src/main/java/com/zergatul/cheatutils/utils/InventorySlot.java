package com.zergatul.cheatutils.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class InventorySlot {

    private EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
    private int slot = -1;

    public InventorySlot(int slot) {
        this.slot = slot;
    }

    public InventorySlot(EquipmentSlot equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    public ItemStack get() {
        if (slot >= 0) {
            return MinecraftClient.getInstance().player.getInventory().main.get(slot);
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            return MinecraftClient.getInstance().player.getInventory().offHand.get(0);
        }
        return null;
    }

    public void set(ItemStack itemStack) {
        if (slot >= 0) {
            MinecraftClient.getInstance().player.getInventory().main.set(slot, itemStack.copy());
            return;
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            MinecraftClient.getInstance().player.getInventory().offHand.set(0, itemStack.copy());
            return;
        }
    }

    public int toServer() {
        if (slot >= 0) {
            return slot < 9 ? slot + 36 : slot;
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            return 45;
        }
        return -1;
    }
}