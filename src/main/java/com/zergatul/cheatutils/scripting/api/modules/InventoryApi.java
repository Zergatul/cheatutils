package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryApi {

    private final static Minecraft mc = Minecraft.getInstance();

    public String getHeadItemId() {
        return getItemBySlot(EquipmentSlot.HEAD);
    }

    public String getChestItemId() {
        return getItemBySlot(EquipmentSlot.CHEST);
    }

    public String getLegsItemId() {
        return getItemBySlot(EquipmentSlot.LEGS);
    }

    public String getFeetItemId() {
        return getItemBySlot(EquipmentSlot.FEET);
    }

    @ApiVisibility(ApiType.ACTION)
    public boolean equip(String itemId) {
        if (mc.player == null) {
            return false;
        }

        Item item = ModApiWrapper.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            return false;
        }

        Inventory inventory = mc.player.getInventory();
        int index = -1;
        ItemStack itemStack = null;
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).is(item)) {
                index = i;
                itemStack = inventory.getItem(i);
                break;
            }
        }
        if (itemStack == null) {
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).is(item)) {
                    index = i;
                    itemStack = inventory.getItem(i);
                    break;
                }
            }
        }

        if (itemStack == null) {
            return false;
        }

        EquipmentSlot slot = Mob.getEquipmentSlotForItem(itemStack);
        if (slot == EquipmentSlot.MAINHAND) {
            return false;
        }

        InventoryUtils.moveItemStack(new InventorySlot(index), new InventorySlot(slot));
        return true;
    }

    private String getItemBySlot(EquipmentSlot slot) {
        if (mc.player == null) {
            return "";
        }

        ItemStack itemStack = mc.player.getItemBySlot(slot);
        if (itemStack.isEmpty()) {
            return "";
        }

        return ModApiWrapper.ITEMS.getKey(itemStack.getItem()).toString();
    }

    private int find(String itemId) {
        if (mc.player == null) {
            return -1;
        }

        Item item = ModApiWrapper.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            return -1;
        }

        Inventory inventory = mc.player.getInventory();
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).is(item)) {
                return i;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).is(item)) {
                return i;
            }
        }

        return -1;
    }
}