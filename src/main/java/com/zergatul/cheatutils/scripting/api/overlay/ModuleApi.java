package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ModuleConfig;

public abstract class ModuleApi<T extends ModuleConfig> {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    protected abstract T getConfig();
}