package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoToolConfig;
import com.zergatul.cheatutils.modules.automation.AutoTool;

public class AutoToolApi extends SettingHandleApi<AutoToolConfig> {

    public AutoToolApi() {
        super(AutoTool.instance.getSettings());
    }
}