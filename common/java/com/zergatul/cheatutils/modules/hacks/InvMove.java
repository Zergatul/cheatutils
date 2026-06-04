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

    public void onKeyPress(int action, KeyEvent event) {
        if (!shouldPassEvents(mc.screen)) {
            return;
        }

        InputConstants.Key key = InputConstants.getKey(event);
        if (isContainerScreenKey(key)) {
            return;
        }

        for (KeyMapping mapping : mc.options.keyMappings) {
            if (mapping == mc.options.keyDebugModifier) {
                continue;
            }
            if (((KeyMappingAccessor) mapping).getKey_CU().equals(key)) {
                mapping.setDown(action != 0);
                if (action != 0) {
                    KeyMappingAccessor accessor = (KeyMappingAccessor) mapping;
                    accessor.setClickCount_CU(accessor.getClickCount_CU() + 1);
                }
            }
        }
    }

    public boolean isContainerScreenKey(InputConstants.Key key) {
        if (((KeyMappingAccessor) mc.options.keyDebugModifier).getKey_CU().equals(key)) {
            return false;
        }

        for (KeyMapping mapping : mc.options.keyMappings) {
            if (isContainerScreenKey(mapping) && ((KeyMappingAccessor) mapping).getKey_CU().equals(key)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldIgnoreForgeKeyContext(KeyMapping mapping) {
        return shouldIgnoreKeyContext(mapping);
    }

    public boolean shouldIgnoreKeyContext(KeyMapping mapping) {
        return shouldPassEvents(mc.screen) && isRegisteredKeyMapping(mapping);
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

    private boolean isContainerScreenKey(KeyMapping mapping) {
        if (mapping == mc.options.keyInventory) {
            return true;
        }
        if (mapping == mc.options.keyDrop) {
            return true;
        }
        if (mapping == mc.options.keySwapOffhand) {
            return true;
        }
        if (mapping == mc.options.keyPickItem) {
            return true;
        }
        for (KeyMapping hotbarMapping : mc.options.keyHotbarSlots) {
            if (mapping == hotbarMapping) {
                return true;
            }
        }
        return false;
    }

    private boolean isRegisteredKeyMapping(KeyMapping mapping) {
        if (mapping == mc.options.keyDebugModifier) {
            return false;
        }
        for (KeyMapping registered : mc.options.keyMappings) {
            if (registered == mapping) {
                return true;
            }
        }
        return false;
    }
}