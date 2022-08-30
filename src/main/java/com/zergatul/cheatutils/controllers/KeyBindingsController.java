package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.wrappers.IKeyBindingRegistry;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyBindingsController {

    public static final KeyBindingsController instance = new KeyBindingsController();

    public final KeyMapping[] keys;

    private Minecraft mc = Minecraft.getInstance();
    private Runnable[] actions = new Runnable[KeyBindingsConfig.KeysCount];

    private KeyBindingsController() {
        keys = new KeyMapping[KeyBindingsConfig.KeysCount];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new KeyMapping("key.zergatul.cheatutils.reserved" + i, InputConstants.UNKNOWN.getValue(), "category.zergatul.cheatutils");
        }

        ModApiWrapper.addOnRegisterKeyBindings(this::onRegisterKeyBindings);
        ModApiWrapper.addOnKeyInput(this::onKeyInputEvent);
    }

    public void assign(int index, String name) {
        String[] bindings = ConfigStore.instance.getConfig().keyBindingsConfig.bindings;
        for (int i = 0; i < bindings.length; i++) {
            if (bindings[i] != null && bindings[i].equals(name)) {
                actions[i] = null;
                bindings[i] = null;
            }
        }

        if (0 <= index && index < KeyBindingsConfig.KeysCount) {
            Runnable compiled = ScriptController.instance.get(name);
            if (compiled == null) {
                actions[index] = null;
                bindings[index] = null;
            } else {
                actions[index] = compiled;
                bindings[index] = name;
            }
        }
    }

    private void onKeyInputEvent() {
        if (mc.player == null) {
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isDown() && actions[i] != null) {
                actions[i].run();
            }
        }
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (int i = 0; i < keys.length; i++) {
            registry.register(keys[i]);
        }
    }
}