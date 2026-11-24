package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoCraftConfig;
import com.zergatul.cheatutils.modules.automation.AutoCraft;

public class AutoCraftApi extends SettingHandleApi<AutoCraftConfig> {
    public AutoCraftApi() {
        super(AutoCraft.INSTANCE.getSettings());
    }
}