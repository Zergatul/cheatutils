package com.zergatul.cheatutils.modules.scripting;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyBindings implements Module {

    public static final KeyBindings instance = new KeyBindings();

    public final KeyMapping[] keys;

    private final Minecraft mc = Minecraft.getInstance();
    private final Runnable[] actions = new Runnable[KeyBindingsConfig.KeysCount];

    private KeyBindings() {
        keys = new KeyMapping[KeyBindingsConfig.KeysCount];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new KeyMapping("key.zergatul.cheatutils.reserved" + i, InputConstants.UNKNOWN.getValue(), "category.zergatul.cheatutils");
        }

        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);
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

    private void onHandleKeyBindings() {
        if (mc.player == null) {
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            KeyMapping key = keys[i];
            Runnable action = actions[i];
            while (key.consumeClick()) {
                if (action != null) {
                    action.run();
                }
            }
        }
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (int i = 0; i < keys.length; i++) {
            registry.register(keys[i]);
        }
    }
}