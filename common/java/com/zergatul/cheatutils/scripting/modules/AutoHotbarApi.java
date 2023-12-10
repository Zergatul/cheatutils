package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoHotbarConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoHotbarApi extends ModuleApi<AutoHotbarConfig> {

    @Override
    protected AutoHotbarConfig getConfig() {
        return ConfigStore.instance.getConfig().autoHotbarConfig;
    }
}