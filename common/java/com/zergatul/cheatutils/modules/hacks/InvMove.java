package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class InvMove implements Module {

    public static final InvMove instance = new InvMove();

    private InvMove() {}

    public Screen overrideCurrentScreen(Screen screen) {
        if (screen != null && InvMove.instance.shouldPassEvents(screen)) {
            return null;
        } else {
            return screen;
        }
    }

    public boolean shouldPassEvents(Screen screen) {
        return ConfigStore.instance.getConfig().invMoveConfig.enabled && isValidScreen(screen);
    }

    private boolean isValidScreen(Screen screen) {
        return screen instanceof AbstractContainerScreen;
    }
}