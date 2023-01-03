package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FogConfig;

public class FogApi {
    public void toggle() {
        FogConfig config = ConfigStore.instance.getConfig().fogConfig;
        config.disableFog = !config.disableFog;
        ConfigStore.instance.requestWrite();
    }
}