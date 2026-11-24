package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.PigHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class PigHack implements Module {

    public static final PigHack INSTANCE = new PigHack();

    private final SettingHandle<PigHackConfig> settings = Settings.get(PigHackConfig.class);

    private PigHack() {
        WebApiRegistry.INSTANCE.register("pig-hack", settings, PigHackConfig.class);
    }

    public SettingHandle<PigHackConfig> getSettings() {
        return settings;
    }
}