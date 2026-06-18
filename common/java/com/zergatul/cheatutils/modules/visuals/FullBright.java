package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;

public class FullBright implements Module {

    public static final FullBright instance = new FullBright();

    private FullBright() {}

    public boolean shouldFakeNighVision() {
        return ConfigStore.instance.getConfig().fullBrightConfig.enabled;
    }
}