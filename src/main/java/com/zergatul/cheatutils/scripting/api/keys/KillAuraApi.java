package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;

public class KillAuraApi {

    public boolean isEnabled() {
        var config = getConfig();
        return config.active;
    }

    public void toggle() {
        var config = getConfig();
        config.active = !config.active;
        ConfigStore.instance.requestWrite();
    }

    private KillAuraConfig getConfig() {
        return ConfigStore.instance.getConfig().killAuraConfig;
    }
}