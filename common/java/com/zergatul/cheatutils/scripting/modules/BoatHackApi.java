package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.BoatHackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class BoatHackApi {

    public boolean isFlyEnabled() {
        return getConfig().fly;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleFly() {
        BoatHackConfig config = getConfig();
        config.fly = !config.fly;
        ConfigStore.instance.requestWrite();
    }

    private BoatHackConfig getConfig() {
        return ConfigStore.instance.getConfig().boatHackConfig;
    }
}