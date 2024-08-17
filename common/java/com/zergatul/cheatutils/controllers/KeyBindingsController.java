package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;

public class KeyBindingsController {

    public static final KeyBindingsController instance = new KeyBindingsController();

    public final KeyMapping[] keys;

    private final Minecraft mc = Minecraft.getInstance();
    private final AsyncRunnable[] actions = new AsyncRunnable[KeyBindingsConfig.KeysCount];
    private final CompletableFuture<?>[] futures = new CompletableFuture<?>[KeyBindingsConfig.KeysCount];

    private KeyBindingsController() {
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
            AsyncRunnable compiled = ScriptsController.instance.get(name);
            if (compiled == null) {
                actions[index] = null;
                futures[index] = null;
                bindings[index] = null;
            } else {
                actions[index] = compiled;
                futures[index] = null;
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
            AsyncRunnable action = actions[i];
            while (key.consumeClick()) {
                if (action != null && (futures[i] == null || futures[i].isDone())) {
                    futures[i] = action.run();
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