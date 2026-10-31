package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.KillAuraConfig;
import com.zergatul.cheatutils.modules.hacks.KillAura;

public class KillAuraApi extends SettingHandle2Api<KillAuraConfig> {
    public KillAuraApi() {
        super(KillAura.INSTANCE.getSettings());
    }
}