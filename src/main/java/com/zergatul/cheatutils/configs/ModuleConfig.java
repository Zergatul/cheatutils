package com.zergatul.cheatutils.configs;

public abstract class ModuleConfig {
    public boolean enabled;

    public void copyTo(ModuleConfig other) {
        other.enabled = enabled;
    }
}