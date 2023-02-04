package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NoFallConfig;

public class NoFallApi extends ModuleApi<NoFallConfig> {

    @Override
    protected NoFallConfig getConfig() {
        return ConfigStore.instance.getConfig().noFallConfig;
    }
}