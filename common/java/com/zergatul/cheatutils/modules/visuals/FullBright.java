package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FullBrightConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FullBright implements Module {

    public static final FullBright instance = new FullBright();

    private final ModuleConfigSettingHandle<FullBrightConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().fullBrightConfig,
            config -> ConfigStore.instance.getConfig().fullBrightConfig = config);

    private FullBright() {
        WebApiRegistry.INSTANCE.register("full-bright", settings, FullBrightConfig.class);
    }

    public ModuleConfigSettingHandle<FullBrightConfig> getSettings() {
        return settings;
    }

    public boolean shouldFakeNighVision() {
        return settings.get().enabled;
    }
}