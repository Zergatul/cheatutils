package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class CrystalAuraApi extends ModuleApi<CrystalAuraConfig> {


    @Override
    protected CrystalAuraConfig getConfig() {
        return ConfigStore.instance.getConfig().crystalAuraConfig;
    }
}