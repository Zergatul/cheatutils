package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.StepUpConfig;

public class StepUpApi extends ModuleApi<StepUpConfig> {

    @Override
    protected StepUpConfig getConfig() {
        return ConfigStore.instance.getConfig().stepUp;
    }
}
