package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoCraftConfig;
import com.zergatul.cheatutils.modules.automation.AutoCraft;

public class AutoCraftApi extends SettingHandleApi<AutoCraftConfig> {

    public AutoCraftApi() {
        super(AutoCraft.instance.getSettings());
    }
}