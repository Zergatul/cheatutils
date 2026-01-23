package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoStunnerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoStunnerApi extends ModuleApi<AutoStunnerConfig> {

    @Override
    protected AutoStunnerConfig getConfig() {
        return ConfigStore.instance.getConfig().autoStunnerConfig;
    }
}