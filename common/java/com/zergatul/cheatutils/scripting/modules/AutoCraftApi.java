package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoCraftConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoCraftApi extends ModuleApi<AutoCraftConfig> {

    @Override
    protected AutoCraftConfig getConfig() {
        return ConfigStore.instance.getConfig().autoCraftConfig;
    }
}