package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.AntiRespawnResetConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AntiRespawnReset implements Module {

    public static final AntiRespawnReset INSTANCE = new AntiRespawnReset();

    private final ModuleConfigSettingHandle<AntiRespawnResetConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().antiRespawnResetConfig,
            config -> ConfigStore.instance.getConfig().antiRespawnResetConfig = config);

    private AntiRespawnReset() {
        WebApiRegistry.INSTANCE.register("anti-respawn-reset", settings, AntiRespawnResetConfig.class);
    }

    public ModuleConfigSettingHandle<AntiRespawnResetConfig> getSettings() {
        return settings;
    }
}