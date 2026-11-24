package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.PerformanceConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Performance implements Module {

    public static final Performance INSTANCE = new Performance();

    private final SettingHandle<PerformanceConfig> settings = Settings.get(PerformanceConfig.class);

    private Performance() {
        WebApiRegistry.INSTANCE.register("performance", settings, PerformanceConfig.class);
    }

    public SettingHandle<PerformanceConfig> getSettings() {
        return settings;
    }
}