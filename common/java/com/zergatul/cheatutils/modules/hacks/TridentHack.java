package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.TridentHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class TridentHack implements Module {

    public static final TridentHack INSTANCE = new TridentHack();

    private final SettingHandle2<TridentHackConfig> settings = Settings.get(TridentHackConfig.class);

    private TridentHack() {
        WebApiRegistry.INSTANCE.register("trident-hack", settings, TridentHackConfig.class);
    }

    public boolean shouldOverrideIsInWater() {
        TridentHackConfig config = settings.get();
        return config.enabled() && config.allowOutOfWater();
    }

    public double getPushMultiplier() {
        TridentHackConfig config = settings.get();
        return config.enabled() ? config.boost() : 1;
    }
}