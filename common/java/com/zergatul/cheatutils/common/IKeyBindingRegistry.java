package com.zergatul.cheatutils.common;

import net.minecraft.client.KeyMapping;

public interface IKeyBindingRegistry {
    void register(KeyMapping key);
}