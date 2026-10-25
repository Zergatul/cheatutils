package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.NoFallConfig;
import com.zergatul.cheatutils.modules.hacks.NoFall;

public class NoFallApi extends SettingHandle2Api<NoFallConfig> {
    public NoFallApi() {
        super(NoFall.INSTANCE.getSettings());
    }
}