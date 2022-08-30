package com.zergatul.cheatutils.wrappers;

import net.minecraft.client.option.KeyBinding;

public interface IKeyBindingRegistry {
    void register(KeyBinding key);
}