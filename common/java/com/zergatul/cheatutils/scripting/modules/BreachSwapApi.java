package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.BreachSwapConfig;
import com.zergatul.cheatutils.modules.automation.BreachSwap;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class BreachSwapApi extends SettingHandleApi<BreachSwapConfig> {

    public BreachSwapApi() {
        super(BreachSwap.instance.getSettings());
    }
}