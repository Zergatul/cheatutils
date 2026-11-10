package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AutoDropConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AutoDrop implements Module {

    public static final AutoDrop INSTANCE = new AutoDrop();

    private final SettingHandle<AutoDropConfig> settings = Settings.get(AutoDropConfig.class);
    private AutoDrop() {
        WebApiRegistry.INSTANCE.register("auto-drop", settings, AutoDropConfig.class);
    }

    public SettingHandle<AutoDropConfig> getSettings() {
        return settings;
    }
}