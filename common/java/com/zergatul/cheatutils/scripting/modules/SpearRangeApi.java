package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SpearRangeConfig;

public class SpearRangeApi extends ModuleApi<SpearRangeConfig> {


    @Override
    protected SpearRangeConfig getConfig() {
        return ConfigStore.instance.getConfig().spearRangeConfig;
    }
}