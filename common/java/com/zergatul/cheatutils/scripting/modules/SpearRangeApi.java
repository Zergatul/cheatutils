package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.SpearRangeConfig;
import com.zergatul.cheatutils.modules.automation.SpearRange;

public class SpearRangeApi extends SettingHandleApi<SpearRangeConfig> {

    public SpearRangeApi() {
        super(SpearRange.instance.getSettings());
    }
}