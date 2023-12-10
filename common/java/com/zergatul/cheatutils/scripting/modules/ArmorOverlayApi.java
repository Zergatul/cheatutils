package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ArmorOverlayConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class ArmorOverlayApi extends ModuleApi<ArmorOverlayConfig> {

    @Override
    protected ArmorOverlayConfig getConfig() {
        return ConfigStore.instance.getConfig().armorOverlayConfig;
    }
}