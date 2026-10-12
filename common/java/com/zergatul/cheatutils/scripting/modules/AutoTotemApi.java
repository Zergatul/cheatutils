package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoTotemConfig;
import com.zergatul.cheatutils.modules.automation.AutoTotem;

public class AutoTotemApi extends SettingHandleApi<AutoTotemConfig> {

    public AutoTotemApi() {
        super(AutoTotem.instance.getSettings());
    }
}