package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;

public class FastBreakApi extends ModuleApi<FastBreakConfig> {

    @Override
    protected FastBreakConfig getConfig() {
        return ConfigStore.instance.getConfig().fastBreakConfig;
    }
}