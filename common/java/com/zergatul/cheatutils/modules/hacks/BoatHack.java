package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.BoatHackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BoatHack implements Module {

    public static final BoatHack INSTANCE = new BoatHack();

    private final SettingHandle<BoatHackConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().boatHackConfig,
            config -> ConfigStore.instance.getConfig().boatHackConfig = config);

    private BoatHack() {
        WebApiRegistry.INSTANCE.register("boat-hack", settings, BoatHackConfig.class);
    }

    public SettingHandle<BoatHackConfig> getSettings() {
        return settings;
    }
}