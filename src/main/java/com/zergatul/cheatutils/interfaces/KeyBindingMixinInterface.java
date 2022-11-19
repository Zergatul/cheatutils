package com.zergatul.cheatutils.interfaces;

import net.minecraft.client.util.InputUtil;

public interface KeyBindingMixinInterface {
    InputUtil.Key getBoundKey();
}