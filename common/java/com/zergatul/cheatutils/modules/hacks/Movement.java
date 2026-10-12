package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Movement implements Module {

    public static final Movement INSTANCE = new Movement();

    private final SettingHandle<MovementHackConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().movementHackConfig,
            config -> ConfigStore.instance.getConfig().movementHackConfig = config);

    private Movement() {
        WebApiRegistry.INSTANCE.register("movement-hack", settings, MovementHackConfig.class);
    }

    public SettingHandle<MovementHackConfig> getSettings() {
        return settings;
    }
}