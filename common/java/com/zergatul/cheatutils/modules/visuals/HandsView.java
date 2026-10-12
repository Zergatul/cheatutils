package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HandsViewConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class HandsView implements Module {

    public static final HandsView INSTANCE = new HandsView();

    private final ModuleConfigSettingHandle<HandsViewConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().handsViewConfig,
            config -> ConfigStore.instance.getConfig().handsViewConfig = config);

    private HandsView() {
        WebApiRegistry.INSTANCE.register("hands-view", settings, HandsViewConfig.class);
    }

    public ModuleConfigSettingHandle<HandsViewConfig> getSettings() {
        return settings;
    }
}