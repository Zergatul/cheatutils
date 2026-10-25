package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoStunnerConfig;
import com.zergatul.cheatutils.modules.automation.AutoStunner;

public class AutoStunnerApi extends SettingHandle2Api<AutoStunnerConfig> {
    public AutoStunnerApi() {
        super(AutoStunner.INSTANCE.getSettings());
    }
}