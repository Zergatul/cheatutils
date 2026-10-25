package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.SpearRangeConfig;
import com.zergatul.cheatutils.modules.automation.SpearRange;

public class SpearRangeApi extends SettingHandle2Api<SpearRangeConfig> {
    public SpearRangeApi() {
        super(SpearRange.INSTANCE.getSettings());
    }
}