package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LightLevelConfig;
import com.zergatul.cheatutils.modules.esp.LightLevel;

public class LightLevelApi extends ModuleApi<LightLevelConfig> {

    @Override
    protected LightLevelConfig getConfig() {
        return ConfigStore.instance.getConfig().lightLevelConfig;
    }

    @Override
    protected void onEnableChanged() {
        LightLevel.instance.onChanged();
    }
}