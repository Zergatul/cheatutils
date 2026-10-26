package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoHotbarConfig;
import com.zergatul.cheatutils.modules.automation.AutoHotbar;

public class AutoHotbarApi extends SettingHandle2Api<AutoHotbarConfig> {
    public AutoHotbarApi() {
        super(AutoHotbar.INSTANCE.getSettings());
    }
}