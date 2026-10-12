package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.LightLevelConfig;
import com.zergatul.cheatutils.modules.esp.LightLevel;

public class LightLevelApi extends SettingHandleApi<LightLevelConfig> {
    public LightLevelApi() {
        super(LightLevel.INSTANCE.getSettings());
    }
}