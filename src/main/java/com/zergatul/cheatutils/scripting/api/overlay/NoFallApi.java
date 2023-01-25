package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;

public class NoFallApi {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    private NoFallConfig getConfig() {
        return ConfigStore.instance.getConfig().noFallConfig;
    }
}