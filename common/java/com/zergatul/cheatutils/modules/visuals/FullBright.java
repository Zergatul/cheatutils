package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.FullBrightConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FullBright implements Module {

    public static final FullBright INSTANCE = new FullBright();

    private final SettingHandle<FullBrightConfig> settings = Settings.get(FullBrightConfig.class);

    private FullBright() {
        WebApiRegistry.INSTANCE.register("full-bright", settings, FullBrightConfig.class);
    }

    public SettingHandle<FullBrightConfig> getSettings() {
        return settings;
    }

    public boolean shouldFakeNighVision() {
        return settings.get().enabled();
    }
}