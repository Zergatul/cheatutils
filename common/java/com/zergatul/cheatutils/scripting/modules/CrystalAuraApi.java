package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.CrystalAuraConfig;
import com.zergatul.cheatutils.modules.hacks.CrystalAura;

public class CrystalAuraApi extends SettingHandle2Api<CrystalAuraConfig> {
    public CrystalAuraApi() {
        super(CrystalAura.INSTANCE.getSettings());
    }
}