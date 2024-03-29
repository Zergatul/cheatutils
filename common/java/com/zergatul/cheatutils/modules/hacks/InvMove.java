package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class InvMove implements Module {

    public static final InvMove instance = new InvMove();

    private InvMove() {

    }

    public Screen overrideGetScreen(Minecraft mc) {
        if (mc.screen != null && InvMove.instance.shouldPassEvents(mc.screen)) {
            return null;
        } else {
            return mc.screen;
        }
    }

    public boolean shouldPassEvents(Screen screen) {
        return ConfigStore.instance.getConfig().invMoveConfig.enabled && isValidScreen(screen);
    }

    private boolean isValidScreen(Screen screen) {
        if (screen instanceof AbstractContainerScreen) {
            return true;
        }
        return false;
    }
}