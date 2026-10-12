package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.KillAuraConfig;
import com.zergatul.cheatutils.modules.hacks.KillAura;

public class KillAuraApi extends SettingHandleApi<KillAuraConfig> {
    public KillAuraApi() {
        super(KillAura.INSTANCE.getSettings());
    }
}