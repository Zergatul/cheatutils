package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.configs.StepUpConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class StepUp implements Module {

    public static final StepUp INSTANCE = new StepUp();

    private final ModuleConfigSettingHandle<StepUpConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().stepUp,
            config -> ConfigStore.instance.getConfig().stepUp = config);

    private StepUp() {
        WebApiRegistry.INSTANCE.register("step-up", settings, StepUpConfig.class);
    }

    public ModuleConfigSettingHandle<StepUpConfig> getSettings() {
        return settings;
    }
}