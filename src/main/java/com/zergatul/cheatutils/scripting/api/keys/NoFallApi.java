package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;

public class NoFallApi {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private NoFallConfig getConfig() {
        return ConfigStore.instance.getConfig().noFallConfig;
    }
}