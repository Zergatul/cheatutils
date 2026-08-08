package com.zergatul.cheatutils.configs;

import java.util.Arrays;
import java.util.Objects;

public class KeyBindingsConfig implements Sanitizable, ModuleStateProvider {

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

    @Override
    public boolean isEnabled() {
        return Arrays.stream(bindings).anyMatch(Objects::nonNull);
    }
}