package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.interfaces.KeyBindingMixinInterface;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyUtils {

    public static void click(KeyBinding binding) {
        InputUtil.Key key = ((KeyBindingMixinInterface) binding).getBoundKey();
        KeyBinding.onKeyPressed(key);
    }
}
