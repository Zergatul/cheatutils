package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;

public class KillAuraApi extends ModuleApi<KillAuraConfig> {

    @Override
    protected KillAuraConfig getConfig() {
        return ConfigStore.instance.getConfig().killAuraConfig;
    }
}