package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AreaMineConfig;
import com.zergatul.cheatutils.modules.hacks.AreaMine;

public class AreaMineApi extends SettingHandle2Api<AreaMineConfig> {
    public AreaMineApi() {
        super(AreaMine.INSTANCE.getSettings());
    }
}