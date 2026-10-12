package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Reach implements Module {

    public static final Reach INSTANCE = new Reach();

    private final SettingHandle<ReachConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().reachConfig,
            config -> ConfigStore.instance.getConfig().reachConfig = config);

    private Reach() {
        WebApiRegistry.INSTANCE.register("reach", settings, ReachConfig.class);
    }

    public SettingHandle<ReachConfig> getSettings() {
        return settings;
    }
}