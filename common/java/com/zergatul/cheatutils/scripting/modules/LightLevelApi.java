package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.LightLevelConfig;
import com.zergatul.cheatutils.modules.esp.LightLevel;

public class LightLevelApi extends SettingHandle2Api<LightLevelConfig> {
    public LightLevelApi() {
        super(LightLevel.INSTANCE.getSettings());
    }
}