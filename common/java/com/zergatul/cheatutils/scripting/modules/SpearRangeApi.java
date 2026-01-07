package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.SpearRangeConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class SpearRangeApi extends ModuleApi<SpearRangeConfig> {


    @Override
    protected SpearRangeConfig getConfig() {
        return ConfigStore.instance.getConfig().spearRangeConfig;
    }
}