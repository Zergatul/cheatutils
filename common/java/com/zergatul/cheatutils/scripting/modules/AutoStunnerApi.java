package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoStunnerConfig;
import com.zergatul.cheatutils.modules.automation.AutoStunner;

public class AutoStunnerApi extends SettingHandleApi<AutoStunnerConfig> {

    public AutoStunnerApi() {
        super(AutoStunner.instance.getSettings());
    }
}