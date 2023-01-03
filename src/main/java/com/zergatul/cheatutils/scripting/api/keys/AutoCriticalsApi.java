package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.AutoCriticalsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoCriticalsApi {
    public void toggle() {
        AutoCriticalsConfig config = ConfigStore.instance.getConfig().autoCriticalsConfig;
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }
}