package com.zergatul.cheatutils.configs;

public abstract class ModuleConfig implements ModuleStateProvider {

    public boolean enabled;

    public void copyTo(ModuleConfig other) {
        other.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}