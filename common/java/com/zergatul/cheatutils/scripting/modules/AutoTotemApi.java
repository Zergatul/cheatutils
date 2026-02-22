package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoTotemConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoTotemApi extends ModuleApi<AutoTotemConfig> {

    @Override
    protected AutoTotemConfig getConfig() {
        return ConfigStore.instance.getConfig().autoTotemConfig;
    }
}