package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoCriticalsConfig;
import com.zergatul.cheatutils.modules.hacks.AutoCriticals;

public class AutoCriticalsApi extends SettingHandle2Api<AutoCriticalsConfig> {
    public AutoCriticalsApi() {
        super(AutoCriticals.INSTANCE.getSettings());
    }
}