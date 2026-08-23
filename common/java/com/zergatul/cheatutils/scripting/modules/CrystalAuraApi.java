package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.modules.hacks.CrystalAura;

public class CrystalAuraApi extends ModuleApi<CrystalAuraConfig> {

    @Override
    protected void onEnableChanged() {
        super.onEnableChanged();
        CrystalAura.instance.onEnableStateChanged();
    }

    @Override
    protected CrystalAuraConfig getConfig() {
        return ConfigStore.instance.getConfig().crystalAuraConfig;
    }
}