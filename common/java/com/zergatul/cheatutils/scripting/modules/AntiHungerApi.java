package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AntiHungerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AntiHungerApi extends ModuleApi<AntiHungerConfig> {

    @Override
    protected AntiHungerConfig getConfig() {
        return ConfigStore.instance.getConfig().antiHungerConfig;
    }
}