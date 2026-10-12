package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.AutoDropConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AutoDrop implements Module {

    public static final AutoDrop INSTANCE = new AutoDrop();

    private final SettingHandle<AutoDropConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().autoDropConfig,
            config -> ConfigStore.instance.getConfig().autoDropConfig = config);
    private AutoDrop() {
        WebApiRegistry.INSTANCE.register("auto-drop", settings, AutoDropConfig.class);
    }

    public SettingHandle<AutoDropConfig> getSettings() {
        return settings;
    }
}