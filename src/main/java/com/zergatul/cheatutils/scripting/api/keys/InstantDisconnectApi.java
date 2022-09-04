package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.InstantDisconnectConfig;

public class InstantDisconnectApi {

    public void toggle() {
        var config = getConfig();
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private InstantDisconnectConfig getConfig() {
        return ConfigStore.instance.getConfig().instantDisconnectConfig;
    }
}
