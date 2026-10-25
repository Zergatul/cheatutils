package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.DeathCoordinatesConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class DeathCoordinates implements Module {

    public static final DeathCoordinates INSTANCE = new DeathCoordinates();

    private final SettingHandle2<DeathCoordinatesConfig> settings = Settings.get(DeathCoordinatesConfig.class);

    private DeathCoordinates() {
        WebApiRegistry.INSTANCE.register("death-coordinates", settings, DeathCoordinatesConfig.class);
    }

    public SettingHandle2<DeathCoordinatesConfig> getSettings() {
        return settings;
    }
}