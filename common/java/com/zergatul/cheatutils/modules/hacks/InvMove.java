package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Input;

public class InvMove implements Module {

    public static final InvMove instance = new InvMove();

    private final Minecraft mc = Minecraft.getInstance();
    private Input storedInputState = Input.EMPTY;

    private InvMove() {}

    public void onOpenScreenStoreKeys(Screen screen) {
        if (!shouldPassEvents(screen)) {
            return;
        }

        storedInputState = new Input(
                mc.options.keyUp.isDown(),
                mc.options.keyDown.isDown(),
                mc.options.keyLeft.isDown(),
                mc.options.keyRight.isDown(),
                mc.options.keyJump.isDown(),
                mc.options.keyShift.isDown(),
                mc.options.keySprint.isDown());
    }

    public void onOpenScreenRestoreKeys(Screen screen) {
        if (!shouldPassEvents(screen)) {
            return;
        }

        mc.options.keyUp.setDown(storedInputState.forward());
        mc.options.keyDown.setDown(storedInputState.backward());
        mc.options.keyLeft.setDown(storedInputState.left());
        mc.options.keyRight.setDown(storedInputState.right());
        mc.options.keyJump.setDown(storedInputState.jump());
        mc.options.keyShift.setDown(storedInputState.shift());
        mc.options.keySprint.setDown(storedInputState.sprint());
    }

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