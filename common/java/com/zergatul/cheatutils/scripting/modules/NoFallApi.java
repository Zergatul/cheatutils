package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.NoFallConfig;
import com.zergatul.cheatutils.modules.hacks.NoFall;

public class NoFallApi extends SettingHandleApi<NoFallConfig> {

    public NoFallApi() {
        super(NoFall.instance.getSettings());
    }
}