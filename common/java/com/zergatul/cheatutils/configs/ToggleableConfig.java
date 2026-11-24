package com.zergatul.cheatutils.configs;

public interface ToggleableConfig<T extends ToggleableConfig<T>> extends ModuleStateProvider {

    boolean enabled();
    T withEnabled(boolean enabled);

    default boolean isEnabled() {
        return enabled();
    }
}