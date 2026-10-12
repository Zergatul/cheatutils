package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoEatConfig;
import com.zergatul.cheatutils.modules.automation.AutoEat;

public class AutoEatApi extends SettingHandleApi<AutoEatConfig> {

    public AutoEatApi() {
        super(AutoEat.instance.getSettings());
    }
}