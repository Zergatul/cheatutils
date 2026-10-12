package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.FastBreakConfig;
import com.zergatul.cheatutils.modules.hacks.FastBreak;

public class FastBreakApi extends SettingHandleApi<FastBreakConfig> {

    public FastBreakApi() {
        super(FastBreak.INSTANCE.getSettings());
    }
}