package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoCriticalsConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoCriticalsApi extends ModuleApi<AutoCriticalsConfig> {

    @Override
    protected AutoCriticalsConfig getConfig() {
        return ConfigStore.instance.getConfig().autoCriticalsConfig;
    }
}