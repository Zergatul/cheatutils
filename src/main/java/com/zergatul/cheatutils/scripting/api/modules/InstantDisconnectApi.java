package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.InstantDisconnectConfig;

public class InstantDisconnectApi extends ModuleApi<InstantDisconnectConfig> {

    @Override
    protected InstantDisconnectConfig getConfig() {
        return ConfigStore.instance.getConfig().instantDisconnectConfig;
    }
}