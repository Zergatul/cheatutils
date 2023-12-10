package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ScaffoldConfig;

public class ScaffoldApi extends ModuleApi<ScaffoldConfig> {

    @Override
    protected ScaffoldConfig getConfig() {
        return ConfigStore.instance.getConfig().scaffoldConfig;
    }
}