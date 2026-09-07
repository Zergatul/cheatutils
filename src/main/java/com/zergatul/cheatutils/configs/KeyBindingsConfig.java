package com.zergatul.cheatutils.configs;

import java.util.Arrays;
import java.util.Objects;

public class KeyBindingsConfig implements Sanitizable, ModuleStateProvider {

    public static final int KEYS_COUNT = 30;

    public String[] bindings = new String[KEYS_COUNT];

    public KeyBindingsConfig() {}

    @Override
    public void sanitize() {
        bindings = bindings == null ? new String[KEYS_COUNT] : Arrays.copyOf(bindings, KEYS_COUNT);
    }

    @Override
    public boolean isEnabled() {
        return Arrays.stream(bindings).anyMatch(Objects::nonNull);
    }
}