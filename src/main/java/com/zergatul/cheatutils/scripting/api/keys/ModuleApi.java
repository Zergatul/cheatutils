package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfig;

public abstract class ModuleApi<T extends ModuleConfig> {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    public void enable() {
        var config = getConfig();
        if (!config.enabled) {
            config.enabled = true;
            ConfigStore.instance.requestWrite();
        }
    }

    public void disable() {
        var config = getConfig();
        if (config.enabled) {
            config.enabled = false;
            ConfigStore.instance.requestWrite();
        }
    }

    public void setEnabled(boolean value) {
        var config = getConfig();
        if (config.enabled != value) {
            config.enabled = value;
            ConfigStore.instance.requestWrite();
        }
    }

    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    protected abstract T getConfig();
}