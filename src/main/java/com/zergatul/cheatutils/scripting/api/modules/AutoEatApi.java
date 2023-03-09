package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.AutoEatConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoEatApi extends ModuleApi<AutoEatConfig> {

    @Override
    protected AutoEatConfig getConfig() {
        return ConfigStore.instance.getConfig().autoEatConfig;
    }
}