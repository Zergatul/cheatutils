package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoToolConfig;
import com.zergatul.cheatutils.modules.automation.AutoTool;

public class AutoToolApi extends SettingHandle2Api<AutoToolConfig> {
    public AutoToolApi() {
        super(AutoTool.INSTANCE.getSettings());
    }
}