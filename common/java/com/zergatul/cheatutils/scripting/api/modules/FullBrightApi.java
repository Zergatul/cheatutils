package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FullBrightConfig;

public class FullBrightApi extends ModuleApi<FullBrightConfig> {

    @Override
    protected FullBrightConfig getConfig() {
        return ConfigStore.instance.getConfig().fullBrightConfig;
    }
}