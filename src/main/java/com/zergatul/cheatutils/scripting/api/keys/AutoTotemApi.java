package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.AutoTotemConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoTotemApi {
    public void toggle() {
        AutoTotemConfig config = ConfigStore.instance.getConfig().autoTotemConfig;
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }
}