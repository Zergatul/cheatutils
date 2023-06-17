package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.AutoHotbarConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.InventorySlot;
import com.zergatul.cheatutils.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoHotbar implements Module {

    public static final AutoHotbar instance = new AutoHotbar();

    private final Minecraft mc = Minecraft.getInstance();
    private ItemStack[] lastTickHotbar = new ItemStack[9];
    private ItemStack lastTickOffhand;

    private AutoHotbar() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
        clear();
    }

    private void onClientTickEnd() {
        if (mc.player == null) {
            clear();
            return;
        }

        AutoHotbarConfig config = ConfigStore.instance.getConfig().autoHotbarConfig;
        if (!config.enabled) {
            clear();
            return;
        }

        Inventory inventory = mc.player.getInventory();
        for (int hbSlot = 0; hbSlot < 9; hbSlot++) {
            if (config.shouldRefill(hbSlot) && !lastTickHotbar[hbSlot].isEmpty()) {
                if (shouldRefill(lastTickHotbar[hbSlot], inventory.getItem(hbSlot))) {
                    int slot = findSameItem(inventory, lastTickHotbar[hbSlot]);
                    if (slot > 0) {
                        InventoryUtils.addItemStack(new InventorySlot(slot), new InventorySlot(hbSlot));
                    }
                }
            }
        }

        if (config.refillSlotOffhand && !lastTickOffhand.isEmpty()) {
            if (shouldRefill(lastTickOffhand, inventory.offhand.get(0))) {
                int slot = findSameItem(inventory, lastTickOffhand);
                if (slot > 0) {
                    InventoryUtils.addItemStack(new InventorySlot(slot), new InventorySlot(EquipmentSlot.OFFHAND));
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            lastTickHotbar[i] = inventory.getItem(i).copy();
        }
        lastTickOffhand = inventory.offhand.get(0).copy();
    }

    private void clear() {
        for (int i = 0; i < 9; i++) {
            lastTickHotbar[i] = ItemStack.EMPTY;
        }
        lastTickOffhand = ItemStack.EMPTY;
    }

    private boolean shouldRefill(ItemStack oldItemStack, ItemStack newItemStack) {
        return newItemStack.isEmpty() || (newItemStack.is(oldItemStack.getItem()) && newItemStack.getCount() <= oldItemStack.getMaxStackSize() / 2);
    }

    private int findSameItem(Inventory inventory, ItemStack search) {
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.isEmpty()) {
                continue;
            }

            if (ItemStack.isSameItemSameTags(itemStack, search)) {
                return i;
            }
        }

        return -1;
    }
}