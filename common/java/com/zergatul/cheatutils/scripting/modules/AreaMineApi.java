package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AreaMineConfig;
import com.zergatul.cheatutils.modules.hacks.AreaMine;

public class AreaMineApi extends SettingHandleApi<AreaMineConfig> {
    public AreaMineApi() {
        super(AreaMine.INSTANCE.getSettings());
    }
}