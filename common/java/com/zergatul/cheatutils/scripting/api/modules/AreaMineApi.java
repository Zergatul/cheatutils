package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.AreaMineConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AreaMineApi extends ModuleApi<AreaMineConfig> {

    @Override
    protected AreaMineConfig getConfig() {
        return ConfigStore.instance.getConfig().areaMineConfig;
    }
}