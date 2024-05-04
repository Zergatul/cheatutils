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
        } else {
            return Minecraft.getInstance().player.getItemBySlot(equipmentSlot);
        }
    }

    public void set(ItemStack itemStack) {
        if (slot >= 0) {
            Minecraft.getInstance().player.getInventory().items.set(slot, itemStack.copy());
        }else {
            Minecraft.getInstance().player.setItemSlot(equipmentSlot, itemStack.copy());
        }
    }

    public int toMenuIndex() {
        if (slot >= 0) {
            return slot < 9 ? slot + 36 : slot;
        } else {
            return switch (equipmentSlot) {
                case HEAD -> 5;
                case CHEST -> 6;
                case LEGS -> 7;
                case FEET -> 8;
                case MAINHAND, BODY -> -1; // ?
                case OFFHAND -> 45;
            };
        }
    }

    public int toInventoryIndex() {
        if (slot >= 0) {
            return slot;
        } else {
            return switch (equipmentSlot) {
                case HEAD -> 39;
                case CHEST -> 38;
                case LEGS -> 37;
                case FEET -> 36;
                case MAINHAND, BODY -> -1; // ?
                case OFFHAND -> 40;
            };
        }
    }
}