package com.zergatul.cheatutils.configs;

import java.util.Arrays;

public class KeyBindingsConfig implements ValidatableConfig {

    public static final int KeysCount = 20;

    public String[] bindings = new String[KeysCount];

    @Override
    public void validate() {
        if (bindings == null) {
            bindings = new String[KeysCount];
        }

        if (bindings.length != KeysCount) {
            bindings = Arrays.copyOf(bindings, KeysCount);
        }
    }
}