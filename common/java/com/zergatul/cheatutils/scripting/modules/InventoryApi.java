package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.MethodDescription;
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

@SuppressWarnings("unused")
public class InventoryApi {

    private final static Minecraft mc = Minecraft.getInstance();

    public String getHeadItemId() {
        return getItemIdBySlot(EquipmentSlot.HEAD);
    }

    public String getChestItemId() {
        return getItemIdBySlot(EquipmentSlot.CHEST);
    }

    public String getLegsItemId() {
        return getItemIdBySlot(EquipmentSlot.LEGS);
    }

    public String getFeetItemId() {
        return getItemIdBySlot(EquipmentSlot.FEET);
    }

    public String getMainHandItemId() {
        return getItemIdBySlot(EquipmentSlot.MAINHAND);
    }

    public String getOffHandItemId() {
        return getItemIdBySlot(EquipmentSlot.OFFHAND);
    }

    @MethodDescription("""
            Returns value in range (0..1]
            """)
    public double getHeadItemDurability() {
        return getItemDurabilityBySlot(EquipmentSlot.HEAD);
    }

    @MethodDescription("""
            Returns value in range (0..1]
            """)
    public double getChestItemDurability() {
        return getItemDurabilityBySlot(EquipmentSlot.CHEST);
    }

    @MethodDescription("""
            Returns value in range (0..1]
            """)
    public double getLegsItemDurability() {
        return getItemDurabilityBySlot(EquipmentSlot.LEGS);
    }

    @MethodDescription("""
            Returns value in range (0..1]
            """)
    public double getFeetItemDurability() {
        return getItemDurabilityBySlot(EquipmentSlot.FEET);
    }

    @MethodDescription("""
            Returns value in range (0..1]
            """)
    public double getMainHandItemDurability() {
        return getItemDurabilityBySlot(EquipmentSlot.MAINHAND);
    }

    @MethodDescription("""
            Returns value in range (0..1]
            """)
    public double getOffHandItemDurability() {
        return getItemDurabilityBySlot(EquipmentSlot.OFFHAND);
    }

    @MethodDescription("""
            Equips item by id to corresponding slot
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean equip(String itemId) {
        if (mc.player == null) {
            return false;
        }

        return equip(itemId, itemStack -> {
            EquipmentSlot slot = mc.player.getEquipmentSlotForItem(itemStack);
            return slot == EquipmentSlot.MAINHAND ? null : slot;
        });
    }

    @MethodDescription("""
            Equips item to offhand
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean equipOffHand(String itemId) {
        return equip(itemId, itemStack -> EquipmentSlot.OFFHAND);
    }

    @MethodDescription("""
            Allows you to equip any item
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipHead(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.HEAD);
    }

    @MethodDescription("""
            Allows you to equip any item
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipChest(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.CHEST);
    }

    @MethodDescription("""
            Allows you to equip any item
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipLegs(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.LEGS);
    }

    @MethodDescription("""
            Allows you to equip any item
            """)
    @ApiVisibility(ApiType.ACTION)
    public boolean swapEquipFeet(String itemId) {
        return equip(itemId, true, itemStack -> EquipmentSlot.FEET);
    }

    @MethodDescription("""
            Returns total amount of items you have in your inventory
            """)
    public int getCount(String itemId) {
        if (mc.player == null) {
            return 0;
        }

        Item item = Registries.ITEMS.getValue(ResourceLocation.parse(itemId));
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

    private String getItemIdBySlot(EquipmentSlot slot) {
        if (mc.player == null) {
            return "";
        }

        ItemStack itemStack = mc.player.getItemBySlot(slot);
        if (itemStack.isEmpty()) {
            return "";
        }

        return Registries.ITEMS.getKey(itemStack.getItem()).toString();
    }

    private double getItemDurabilityBySlot(EquipmentSlot slot) {
        if (mc.player == null) {
            return 1;
        }

        ItemStack itemStack = mc.player.getItemBySlot(slot);
        if (itemStack.isEmpty()) {
            return 1;
        }

        if (!itemStack.isDamaged()) {
            return 1;
        }

        if (itemStack.getMaxDamage() == 0) {
            return 1;
        }

        return 1d - 1d * itemStack.getDamageValue() / itemStack.getMaxDamage();
    }

    private int find(String itemId) {
        if (mc.player == null) {
            return -1;
        }

        Item item = Registries.ITEMS.getValue(ResourceLocation.parse(itemId));
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

        Item item = Registries.ITEMS.getValue(ResourceLocation.parse(itemId));
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