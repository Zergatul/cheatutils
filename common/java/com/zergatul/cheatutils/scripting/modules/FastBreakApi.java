package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.FastBreakConfig;
import com.zergatul.cheatutils.modules.hacks.FastBreak;

public class FastBreakApi extends SettingHandle2Api<FastBreakConfig> {
    public FastBreakApi() {
        super(FastBreak.INSTANCE.getSettings());
    }
}