package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.BreachSwapConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.automation.BreachSwap;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class BreachSwapApi extends ModuleApi<BreachSwapConfig> {
    @ApiVisibility(ApiType.ACTION)
    public void attack(boolean useAxe, boolean breakShield) {
        BreachSwap.instance.attack(useAxe, breakShield);
    }

    @Override
    protected BreachSwapConfig getConfig() {
        return ConfigStore.instance.getConfig().breachSwapConfig;
    }
}