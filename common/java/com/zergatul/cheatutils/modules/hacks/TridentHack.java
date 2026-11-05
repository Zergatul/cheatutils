package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.TridentHackConfig;
import com.zergatul.cheatutils.modules.Module;

public class TridentHack implements Module {

    public static final TridentHack INSTANCE = new TridentHack();

    private TridentHack() {}

    public boolean shouldOverrideIsInWater() {
        TridentHackConfig config = ConfigStore.instance.getConfig().tridentHackConfig;
        return config.enabled && config.allowOutOfWater;
    }

    public double getPushMultiplier() {
        TridentHackConfig config = ConfigStore.instance.getConfig().tridentHackConfig;
        return config.enabled ? config.boost : 1;
    }
}