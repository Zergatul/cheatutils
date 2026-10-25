package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.StepUpConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class StepUp implements Module {

    public static final StepUp INSTANCE = new StepUp();

    private final SettingHandle2<StepUpConfig> settings = Settings.get(StepUpConfig.class);

    private StepUp() {
        WebApiRegistry.INSTANCE.register("step-up", settings, StepUpConfig.class);
    }

    public SettingHandle2<StepUpConfig> getSettings() {
        return settings;
    }
}