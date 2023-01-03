package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;

public class NoFallApi {
    public void toggle() {
        NoFallConfig config = ConfigStore.instance.getConfig().noFallConfig;
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }
}