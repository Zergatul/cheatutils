package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.AutoDisconnectConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoDisconnectApi {

    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private AutoDisconnectConfig getConfig() {
        return ConfigStore.instance.getConfig().autoDisconnectConfig;
    }
}