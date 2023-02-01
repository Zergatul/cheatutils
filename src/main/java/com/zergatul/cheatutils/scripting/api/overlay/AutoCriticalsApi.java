package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.AutoCriticalsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoCriticalsApi {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    private AutoCriticalsConfig getConfig() {
        return ConfigStore.instance.getConfig().autoCriticalsConfig;
    }
}
