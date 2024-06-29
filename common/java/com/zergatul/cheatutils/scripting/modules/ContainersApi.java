package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.modules.scripting.Containers;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HelpText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class ContainersApi {

    private static final Minecraft mc = Minecraft.getInstance();

    public String getScreenTitle() {
        if (mc.screen == null) {
            return "";
        }
        return mc.screen.getTitle().getString();
    }

    public int getMenuId() {
        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return Integer.MIN_VALUE;
        }
        return menu.containerId;
    }

    public String getMenuClass() {
        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return "";
        }
        return menu.getClass().getName();
    }

    public int getSlotsSize() {
        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return Integer.MIN_VALUE;
        }
        return menu.slots.size();
    }

    public boolean isValidSlotIndex(int index) {
        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return false;
        }
        return menu.isValidSlotIndex(index);
    }

    public boolean hasItemAtSlot(int index) {
        Slot slot = getSlot(index);
        return slot != null && slot.hasItem();
    }

    public String getItemIdAtSlot(int index) {
        Slot slot = getSlot(index);
        if (slot == null) {
            return "";
        }
        ItemStack stack = slot.getItem();
        return Registries.ITEMS.getKey(stack.getItem()).toString();
    }

    public int getItemCountAtSlot(int index) {
        Slot slot = getSlot(index);
        if (slot == null) {
            return Integer.MIN_VALUE;
        }
        ItemStack stack = slot.getItem();
        return stack.getCount();
    }

    @HelpText("Button: 0=left, 1=right, 2=middle. ClickType values: PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL")
    @ApiVisibility(ApiType.ACTION)
    public boolean click(int slot, int button, String clickType) {
        if (mc.player == null) {
            return false;
        }
        if (mc.gameMode == null) {
            return false;
        }

        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return false;
        }

        ClickType type;
        try {
            type = ClickType.valueOf(clickType);
        } catch (IllegalArgumentException e) {
            return false;
        }

        mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, button, type, mc.player);
        return true;
    }

    public void waitForOpen(Runnable action) {
        Containers.instance.waitForOpen(action);
    }

    public void waitForNewId(Runnable action) {
        Containers.instance.waitForNewId(action);
    }

    private Slot getSlot(int index) {
        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return null;
        }
        if (index < 0 || index >= menu.slots.size()) {
            return null;
        }
        return menu.getSlot(index);
    }

    private AbstractContainerMenu getContainerMenu() {
        Screen screen = mc.screen;
        if (screen == null) {
            return null;
        }
        if (screen instanceof MenuAccess<?> menuAccess) {
            return menuAccess.getMenu();
        } else {
            return null;
        }
    }
}