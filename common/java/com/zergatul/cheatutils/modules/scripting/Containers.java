package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class Containers implements Module {

    public static final Containers instance = new Containers();

    private static final Minecraft mc = Minecraft.getInstance();
    private static final List<BooleanSupplier> handlers = new ArrayList<>();

    private Containers() {
        Events.ClientTickEnd.add(this::onTickEnd);
        Events.ClientPlayerLoggingOut.add(this::onPlayerLoggingOut);
    }

    public void waitForOpen(Runnable action) {
        if (getContainerMenu() != null) {
            return;
        }

        handlers.add(() -> {
            if (getContainerMenu() != null) {
                action.run();
                return true;
            } else {
                return false;
            }
        });
    }

    public void waitForNewId(Runnable action) {
        AbstractContainerMenu menu = getContainerMenu();
        if (menu == null) {
            return;
        }

        int id = menu.containerId;
        handlers.add(() -> {
            AbstractContainerMenu current = getContainerMenu();
            if (current == null) {
                return true; // stop processing
            }
            if (current.containerId == id) {
                return false; // process in the next tick
            } else {
                action.run();
                return true; // stop processing
            }
        });
    }

    private void onTickEnd() {
        for (int i = 0; i < handlers.size(); i++) {
            if (handlers.get(i).getAsBoolean()) {
                handlers.remove(i);
                i--;
            }
        }
    }

    private void onPlayerLoggingOut() {
        handlers.clear();
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