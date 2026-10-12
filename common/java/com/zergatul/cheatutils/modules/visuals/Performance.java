package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PerformanceConfig;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Performance implements Module {

    public static final Performance INSTANCE = new Performance();

    private final SettingHandle<PerformanceConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().performanceConfig,
            config -> ConfigStore.instance.getConfig().performanceConfig = config);

    private Performance() {
        WebApiRegistry.INSTANCE.register("performance", settings, PerformanceConfig.class);
    }

    public SettingHandle<PerformanceConfig> getSettings() {
        return settings;
    }
}