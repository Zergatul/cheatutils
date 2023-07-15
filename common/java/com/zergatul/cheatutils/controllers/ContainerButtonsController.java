package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import com.zergatul.cheatutils.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerButtonsController {

    public static ContainerButtonsController instance = new ContainerButtonsController();

    private final Minecraft mc = Minecraft.getInstance();

    private ContainerButtonsController() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    public boolean isValidScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        if (!(screen instanceof AbstractContainerScreen<?>)) {
            return false;
        }
        if (screen instanceof EnchantmentScreen) {
            return false;
        }
        if (screen instanceof InventoryScreen) {
            return false;
        }
        if (screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        return true;
    }

    public void dropAll(boolean autoClose) {
        if (!isValidScreen(mc.screen)) {
            return;
        }

        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            ContainerButtonsConfig config = getConfig();
            NonNullList<Slot> slots = screen.getMenu().slots;
            if (slots.size() > 0) {
                if (mc.gameMode == null || mc.player == null) {
                    return;
                }

                Container container = slots.get(0).container;
                for (int i = 0; i < slots.size(); i++) {
                    Slot slot = slots.get(i);
                    if (slot.container != container) {
                        break;
                    }
                    if (slot.getItem().isEmpty()) {
                        continue;
                    }
                    if (config.useFilter && !config.items.contains(slot.getItem().getItem())) {
                        continue;
                    }
                    mc.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, i, 1, ClickType.THROW, mc.player);
                }

                if (autoClose) {
                    screen.onClose();
                }
            }
        }
    }

    public void takeAll(boolean autoClose) {
        if (!isValidScreen(mc.screen)) {
            return;
        }

        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            ContainerButtonsConfig config = getConfig();
            NonNullList<Slot> slots = screen.getMenu().slots;
            if (slots.size() > 0) {
                if (mc.gameMode == null || mc.player == null) {
                    return;
                }

                Container container = slots.get(0).container;
                for (int i = 0; i < slots.size(); i++) {
                    Slot slot = slots.get(i);
                    if (slot.container != container) {
                        break;
                    }
                    if (slot.getItem().isEmpty()) {
                        continue;
                    }
                    if (config.useFilter && !config.items.contains(slot.getItem().getItem())) {
                        continue;
                    }
                    mc.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                }

                if (autoClose) {
                    screen.onClose();
                }
            }
        }
    }

    public void smartPut() {
        if (!isValidScreen(mc.screen)) {
            return;
        }

        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            boolean checkShulkerContent = ConfigStore.instance.getConfig().containerButtonsConfig.checkShulkerContent;

            NonNullList<Slot> slots = screen.getMenu().slots;
            if (slots.size() > 0) {
                if (mc.gameMode == null || mc.player == null) {
                    return;
                }
                Container container = slots.get(0).container;
                List<Item> containerItems = new ArrayList<>();
                Map<Item, List<NonNullList<ItemStack>>> shulkers = new HashMap<>();
                for (int i = 0; i < slots.size(); i++) {
                    Slot slot = slots.get(i);
                    if (slot.getItem().isEmpty()) {
                        continue;
                    }

                    ItemStack itemStack = slot.getItem();
                    Item item = itemStack.getItem();
                    if (slot.container == container) {
                        if (checkShulkerContent && ItemUtils.isShulkerBox(itemStack)) {
                            // add shulker content to list of shulkers
                            if (!shulkers.containsKey(item)) {
                                shulkers.put(item, new ArrayList<>());
                            }
                            shulkers.get(item).add(ItemUtils.getShulkerContent(itemStack));
                        } else {
                            // add item to list if doesn't exist
                            if (!containerItems.contains(item)) {
                                containerItems.add(item);
                            }
                        }
                    } else {
                        if (checkShulkerContent && ItemUtils.isShulkerBox(itemStack)) {
                            if (shulkers.containsKey(item)) {
                                NonNullList<ItemStack> current = ItemUtils.getShulkerContent(itemStack);
                                boolean sameShulker = false;
                                for (NonNullList<ItemStack> existing: shulkers.get(item)) {
                                    // compare content of 2 shulkers
                                    if (existing.size() != current.size()) {
                                        continue;
                                    }

                                    boolean sameItems = true;
                                    for (int j = 0; j < current.size(); j++) {
                                        ItemStack itemStack1 = existing.get(j);
                                        ItemStack itemStack2 = current.get(j);
                                        if (itemStack1.isEmpty() && itemStack2.isEmpty()) {
                                            continue;
                                        }
                                        if (!itemStack1.is(itemStack2.getItem())) {
                                            sameItems = false;
                                            break;
                                        }
                                        if (itemStack1.getCount() != itemStack2.getCount()) {
                                            sameItems = false;
                                            break;
                                        }
                                        if (!ItemStack.tagMatches(itemStack1, itemStack2)) {
                                            sameItems = false;
                                            break;
                                        }
                                    }

                                    if (!sameItems) {
                                        continue;
                                    }

                                    sameShulker = true;
                                    break;
                                }

                                if (sameShulker) {
                                    mc.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                                }
                            }
                        } else {
                            // double click on item if same item exists
                            if (containerItems.contains(item)) {
                                mc.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                            }
                        }
                    }
                }
            }
        }
    }

    private void onClientTickEnd() {
        ContainerButtonsConfig config = getConfig();
        if (config.autoTakeAll || config.autoDropAll) {
            if (!isValidScreen(mc.screen)) {
                return;
            }

            if (config.autoTakeAll) {
                takeAll(config.autoClose);
            } else if (config.autoDropAll) {
                dropAll(config.autoClose);
            }
        }
    }

    private ContainerButtonsConfig getConfig() {
        return ConfigStore.instance.getConfig().containerButtonsConfig;
    }
}