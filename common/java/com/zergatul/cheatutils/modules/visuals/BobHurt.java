package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.configs.modules.BobHurtConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BobHurt implements Module {

    public static final BobHurt INSTANCE = new BobHurt();

    private final SettingHandle<BobHurtConfig> settings = Settings.get(BobHurtConfig.class);

    private BobHurt() {
        WebApiRegistry.INSTANCE.register("bob-hurt", settings, BobHurtConfig.class);
    }

    public SettingHandle<BobHurtConfig> getSettings() {
        return settings;
    }
}