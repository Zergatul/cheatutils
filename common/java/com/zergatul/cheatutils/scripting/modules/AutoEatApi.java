package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoEatConfig;
import com.zergatul.cheatutils.modules.automation.AutoEat;

public class AutoEatApi extends SettingHandleApi<AutoEatConfig> {
    public AutoEatApi() {
        super(AutoEat.INSTANCE.getSettings());
    }
}