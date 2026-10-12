package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.DeathCoordinatesConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class DeathCoordinates implements Module {

    public static final DeathCoordinates INSTANCE = new DeathCoordinates();

    private final ModuleConfigSettingHandle<DeathCoordinatesConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().deathCoordinatesConfig,
            config -> ConfigStore.instance.getConfig().deathCoordinatesConfig = config);

    private DeathCoordinates() {
        WebApiRegistry.INSTANCE.register("death-coordinates", settings, DeathCoordinatesConfig.class);
    }

    public ModuleConfigSettingHandle<DeathCoordinatesConfig> getSettings() {
        return settings;
    }
}