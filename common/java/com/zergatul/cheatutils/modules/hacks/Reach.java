package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.ReachConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Reach implements Module {

    public static final Reach INSTANCE = new Reach();

    private final SettingHandle2<ReachConfig> settings = Settings.get(ReachConfig.class);

    private Reach() {
        WebApiRegistry.INSTANCE.register("reach", settings, ReachConfig.class);
    }

    public SettingHandle2<ReachConfig> getSettings() {
        return settings;
    }
}