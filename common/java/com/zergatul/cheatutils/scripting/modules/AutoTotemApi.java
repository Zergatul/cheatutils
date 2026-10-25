package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoTotemConfig;
import com.zergatul.cheatutils.modules.automation.AutoTotem;

public class AutoTotemApi extends SettingHandle2Api<AutoTotemConfig> {
    public AutoTotemApi() {
        super(AutoTotem.INSTANCE.getSettings());
    }
}