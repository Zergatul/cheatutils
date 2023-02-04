package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ShulkerTooltipConfig;

public class ShulkerTooltipApi extends ModuleApi<ShulkerTooltipConfig> {

    @Override
    protected ShulkerTooltipConfig getConfig() {
        return ConfigStore.instance.getConfig().shulkerTooltipConfig;
    }
}