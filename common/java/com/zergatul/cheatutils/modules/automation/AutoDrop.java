package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AutoDropConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AutoDrop implements Module {

    public static final AutoDrop INSTANCE = new AutoDrop();

    private final SettingHandle2<AutoDropConfig> settings = Settings.get(AutoDropConfig.class);
    private AutoDrop() {
        WebApiRegistry.INSTANCE.register("auto-drop", settings, AutoDropConfig.class);
    }

    public SettingHandle2<AutoDropConfig> getSettings() {
        return settings;
    }
}