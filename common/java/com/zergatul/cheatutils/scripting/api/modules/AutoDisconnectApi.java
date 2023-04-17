package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.AutoDisconnectConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoDisconnectApi extends ModuleApi<AutoDisconnectConfig> {

    @Override
    protected AutoDisconnectConfig getConfig() {
        return ConfigStore.instance.getConfig().autoDisconnectConfig;
    }
}