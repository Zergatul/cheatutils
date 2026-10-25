package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.BreachSwapConfig;
import com.zergatul.cheatutils.modules.automation.BreachSwap;

public class BreachSwapApi extends SettingHandle2Api<BreachSwapConfig> {
    public BreachSwapApi() {
        super(BreachSwap.INSTANCE.getSettings());
    }
}