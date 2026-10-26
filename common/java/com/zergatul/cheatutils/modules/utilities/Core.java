package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.CoreConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Core implements Module {

    public static final Core INSTANCE = new Core();

    private final SettingHandle2<CoreConfig> settings = Settings.get(CoreConfig.class);

    private Core() {
        WebApiRegistry.INSTANCE.register("core", settings, CoreConfig.class);
    }

    public SettingHandle2<CoreConfig> getSettings() {
        return settings;
    }
}