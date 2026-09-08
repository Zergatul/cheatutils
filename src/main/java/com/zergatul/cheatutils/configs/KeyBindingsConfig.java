package com.zergatul.cheatutils.configs;

import java.util.Arrays;

public class KeyBindingsConfig implements Sanitizable {
    public static final int KeysCount = 30;
    public String[] bindings = new String[KeysCount];

    public KeyBindingsConfig() {
        bindings[0] = "Toggle ESP";
        bindings[1] = "Toggle FreeCam";
    }

    @Override
    public void sanitize() {
        bindings = bindings == null ? new String[KeysCount] : Arrays.copyOf(bindings, KeysCount);
    }
}
