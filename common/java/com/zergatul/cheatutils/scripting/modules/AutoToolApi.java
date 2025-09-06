package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoToolConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoToolApi extends ModuleApi<AutoToolConfig> {

    @Override
    protected AutoToolConfig getConfig() {
        return ConfigStore.instance.getConfig().autoTool;
    }
}