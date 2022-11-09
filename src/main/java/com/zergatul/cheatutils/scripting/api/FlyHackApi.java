package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;

public class FlyHackApi {

    public boolean isEnabled() {
        return getConfig().enabled;
    }

    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private FlyHackConfig getConfig() {
        return ConfigStore.instance.getConfig().flyHackConfig;
    }
}
