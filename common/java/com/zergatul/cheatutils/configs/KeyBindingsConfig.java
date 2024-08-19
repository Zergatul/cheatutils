package com.zergatul.cheatutils.configs;

import java.util.Arrays;
import java.util.Objects;

public class KeyBindingsConfig implements ValidatableConfig, ModuleStateProvider {

    public static final int KeysCount = 30;

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

    @Override
    public boolean isEnabled() {
        return Arrays.stream(bindings).anyMatch(Objects::nonNull);
    }
}