package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoAttackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoAttackApi extends ModuleApi<AutoAttackConfig> {

    @Override
    protected AutoAttackConfig getConfig() {
        return ConfigStore.instance.getConfig().autoAttackConfig;
    }
}
