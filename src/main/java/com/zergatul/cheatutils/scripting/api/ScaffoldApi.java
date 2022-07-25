package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ScaffoldConfig;

public class ScaffoldApi {

    public boolean isEnabled() {
        var config = getConfig();
        return config.enabled;
    }

    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
    }

    private ScaffoldConfig getConfig() {
        return ConfigStore.instance.getConfig().scaffoldConfig;
    }
}