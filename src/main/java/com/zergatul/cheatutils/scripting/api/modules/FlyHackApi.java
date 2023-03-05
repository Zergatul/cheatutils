package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;

public class FlyHackApi extends ModuleApi<FlyHackConfig> {

    @Override
    protected FlyHackConfig getConfig() {
        return ConfigStore.instance.getConfig().flyHackConfig;
    }
}