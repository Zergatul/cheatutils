package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfig;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public abstract class ModuleApi<T extends ModuleConfig> {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        var config = getConfig();
        if (!config.enabled) {
            config.enabled = true;
            ConfigStore.instance.requestWrite();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        var config = getConfig();
        if (config.enabled) {
            config.enabled = false;
            ConfigStore.instance.requestWrite();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean value) {
        var config = getConfig();
        if (config.enabled != value) {
            config.enabled = value;
            ConfigStore.instance.requestWrite();
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    protected abstract T getConfig();
}