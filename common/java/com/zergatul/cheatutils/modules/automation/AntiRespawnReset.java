package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AntiRespawnResetConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AntiRespawnReset implements Module {

    public static final AntiRespawnReset INSTANCE = new AntiRespawnReset();

    private final SettingHandle2<AntiRespawnResetConfig> settings = Settings.get(AntiRespawnResetConfig.class);

    private AntiRespawnReset() {
        WebApiRegistry.INSTANCE.register("anti-respawn-reset", settings, AntiRespawnResetConfig.class);
    }

    public SettingHandle2<AntiRespawnResetConfig> getSettings() {
        return settings;
    }
}