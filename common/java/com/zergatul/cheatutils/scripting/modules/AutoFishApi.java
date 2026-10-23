package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoFishConfig;
import com.zergatul.cheatutils.modules.automation.AutoFish;

public class AutoFishApi extends SettingHandle2Api<AutoFishConfig> {
    public AutoFishApi() {
        super(AutoFish.INSTANCE.getSettings());
    }
}