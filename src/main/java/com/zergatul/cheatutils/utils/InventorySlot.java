package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

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
            return Minecraft.getInstance().player.getInventory().items.get(slot);
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            return Minecraft.getInstance().player.getInventory().offhand.get(0);
        }
        return null;
    }

    public void set(ItemStack itemStack) {
        if (slot >= 0) {
            Minecraft.getInstance().player.getInventory().items.set(slot, itemStack.copy());
            return;
        }
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            Minecraft.getInstance().player.getInventory().offhand.set(0, itemStack.copy());
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
