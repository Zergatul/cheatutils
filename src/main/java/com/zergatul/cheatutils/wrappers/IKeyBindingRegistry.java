package com.zergatul.cheatutils.wrappers;

import net.minecraft.client.settings.KeyBinding;

public interface IKeyBindingRegistry {
    void register(KeyBinding key);
}