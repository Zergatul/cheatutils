package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.configs.PigHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class PigHack implements Module {

    public static final PigHack INSTANCE = new PigHack();

    private final ModuleConfigSettingHandle<PigHackConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().pigHackConfig,
            config -> ConfigStore.instance.getConfig().pigHackConfig = config);

    private PigHack() {
        WebApiRegistry.INSTANCE.register("pig-hack", settings, PigHackConfig.class);
    }

    public ModuleConfigSettingHandle<PigHackConfig> getSettings() {
        return settings;
    }
}