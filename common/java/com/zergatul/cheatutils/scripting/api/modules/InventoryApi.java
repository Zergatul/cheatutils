package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

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
        return equip(itemId, itemStack -> {
            EquipmentSlot slot = Mob.getEquipmentSlotForItem(itemStack);
            return slot == EquipmentSlot.MAINHAND ? null : slot;
        });
    }

    @HelpText("Allows you to equip any item")
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipHead(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.HEAD);
    }

    @HelpText("Allows you to equip any item")
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipChest(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.CHEST);
    }

    @HelpText("Allows you to equip any item")
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipLegs(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.LEGS);
    }

    @HelpText("Allows you to equip any item")
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipFeet(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.FEET);
    }

    public int getCount(String itemId) {
        if (mc.player == null) {
            return 0;
        }

        Item item = Registries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            return -1;
        }

        Inventory inventory = mc.player.getInventory();
        int count = 0;
        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.is(item)) {
                count += itemStack.getCount();
            }
        }

        return count;
    }

    private String getItemBySlot(EquipmentSlot slot) {
        if (mc.player == null) {
            return "";
        }

        ItemStack itemStack = mc.player.getItemBySlot(slot);
        if (itemStack.isEmpty()) {
            return "";
        }

        return Registries.ITEMS.getKey(itemStack.getItem()).toString();
    }

    private int find(String itemId) {
        if (mc.player == null) {
            return -1;
        }

        Item item = Registries.ITEMS.getValue(new ResourceLocation(itemId));
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

    private boolean equip(String itemId, Function<ItemStack, EquipmentSlot> getSlot) {
        return equip(itemId, false, getSlot);
    }

    private boolean equip(String itemId, boolean swap, Function<ItemStack, EquipmentSlot> getSlot) {
        if (mc.player == null) {
            return false;
        }

        Item item = Registries.ITEMS.getValue(new ResourceLocation(itemId));
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

        EquipmentSlot slot = getSlot.apply(itemStack);
        if (slot == null) {
            return false;
        }

        if (swap) {
            InventoryUtils.swapItemStack(new InventorySlot(index), new InventorySlot(slot));
        } else {
            InventoryUtils.moveItemStack(new InventorySlot(index), new InventorySlot(slot));
        }

        return true;
    }
}