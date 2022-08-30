package com.zergatul.cheatutils.wrappers;

import net.minecraft.client.KeyMapping;

public interface IKeyBindingRegistry {
    void register(KeyMapping key);
}