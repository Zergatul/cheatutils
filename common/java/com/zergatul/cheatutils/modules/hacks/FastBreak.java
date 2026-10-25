package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.FastBreakConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FastBreak implements Module {

    public static final FastBreak INSTANCE = new FastBreak();

    private final SettingHandle2<FastBreakConfig> settings = Settings.get(FastBreakConfig.class);

    private FastBreak() {
        WebApiRegistry.INSTANCE.register("fast-break", settings, FastBreakConfig.class);
    }

    public SettingHandle2<FastBreakConfig> getSettings() {
        return settings;
    }
}