package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoHotbarConfig;
import com.zergatul.cheatutils.modules.automation.AutoHotbar;

public class AutoHotbarApi extends SettingHandleApi<AutoHotbarConfig> {

    public AutoHotbarApi() {
        super(AutoHotbar.instance.getSettings());
    }
}