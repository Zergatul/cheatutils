package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.AutoEatConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoEatApi {
    public void toggle() {
        AutoEatConfig config = ConfigStore.instance.getConfig().autoEatConfig;
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }
}