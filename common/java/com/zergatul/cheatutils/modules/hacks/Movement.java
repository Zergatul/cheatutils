package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.MovementHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Movement implements Module {

    public static final Movement INSTANCE = new Movement();

    private final SettingHandle2<MovementHackConfig> settings = Settings.get(MovementHackConfig.class);

    private Movement() {
        WebApiRegistry.INSTANCE.register("movement-hack", settings, MovementHackConfig.class);
    }

    public SettingHandle2<MovementHackConfig> getSettings() {
        return settings;
    }
}