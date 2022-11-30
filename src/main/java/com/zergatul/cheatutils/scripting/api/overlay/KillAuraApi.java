package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;

public class KillAuraApi {

    public boolean isEnabled() {
        var config = getConfig();
        return config.active;
    }

    private KillAuraConfig getConfig() {
        return ConfigStore.instance.getConfig().killAuraConfig;
    }
}