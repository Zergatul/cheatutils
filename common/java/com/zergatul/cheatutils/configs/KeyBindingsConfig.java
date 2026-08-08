package com.zergatul.cheatutils.configs;

import java.util.Arrays;

public class KeyBindingsConfig implements Sanitizable {

    public static final int KeysCount = 20;

    public String[] bindings = new String[KeysCount];

    @Override
    public void sanitize() {
        if (bindings == null) {
            bindings = new String[KeysCount];
        }

        if (bindings.length != KeysCount) {
            bindings = Arrays.copyOf(bindings, KeysCount);
        }
    }
}