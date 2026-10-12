package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FastBreak implements Module {

    public static final FastBreak INSTANCE = new FastBreak();

    private final ModuleConfigSettingHandle<FastBreakConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().fastBreakConfig,
            config -> ConfigStore.instance.getConfig().fastBreakConfig = config);

    private FastBreak() {
        WebApiRegistry.INSTANCE.register("fast-break", settings, FastBreakConfig.class);
    }

    public ModuleConfigSettingHandle<FastBreakConfig> getSettings() {
        return settings;
    }
}