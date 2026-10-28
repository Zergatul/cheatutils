package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.HandsViewConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class HandsView implements Module {

    public static final HandsView INSTANCE = new HandsView();

    private final SettingHandle2<HandsViewConfig> settings = Settings.get(HandsViewConfig.class);

    private HandsView() {
        WebApiRegistry.INSTANCE.register("hands-view", settings, HandsViewConfig.class);
    }

    public SettingHandle2<HandsViewConfig> getSettings() {
        return settings;
    }
}