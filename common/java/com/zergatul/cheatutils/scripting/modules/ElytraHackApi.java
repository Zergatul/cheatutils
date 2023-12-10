package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraHackConfig;

public class ElytraHackApi extends ModuleApi<ElytraHackConfig> {

    @Override
    protected ElytraHackConfig getConfig() {
        return ConfigStore.instance.getConfig().elytraHackConfig;
    }
}