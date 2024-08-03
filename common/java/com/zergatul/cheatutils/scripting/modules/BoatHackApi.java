package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.BoatHackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.MethodDescription;

@SuppressWarnings("unused")
public class BoatHackApi {

    @MethodDescription("""
            If Boat Fly enabled
            """)
    public boolean isFlyEnabled() {
        return getConfig().fly;
    }

    @MethodDescription("""
            Toggles Boat Fly status
            """)
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