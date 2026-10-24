package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.BoatHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BoatHack implements Module {

    public static final BoatHack INSTANCE = new BoatHack();

    private final SettingHandle2<BoatHackConfig> settings = Settings.get(BoatHackConfig.class);

    private BoatHack() {
        WebApiRegistry.INSTANCE.register("boat-hack", settings, BoatHackConfig.class);
    }

    public SettingHandle2<BoatHackConfig> getSettings() {
        return settings;
    }
}