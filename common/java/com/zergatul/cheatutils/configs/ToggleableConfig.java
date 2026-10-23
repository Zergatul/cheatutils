package com.zergatul.cheatutils.configs;

public interface ToggleableConfig<T extends ToggleableConfig<T>> {
    boolean enabled();
    T withEnabled(boolean enabled);
}