package com.zergatul.cheatutils.configs;

public interface ToggleableConfig {
    boolean enabled();
    ToggleableConfig withEnabled(boolean enabled);
}