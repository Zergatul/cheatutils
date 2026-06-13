package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;

public class CrystalAuraApi extends ModuleApi<CrystalAuraConfig> {

    @Override
    protected CrystalAuraConfig getConfig() {
        return ConfigStore.instance.getConfig().crystalAuraConfig;
    }
}