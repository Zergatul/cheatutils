package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.FullBrightConfig;
import com.zergatul.cheatutils.modules.visuals.FullBright;

public class FullBrightApi extends SettingHandleApi<FullBrightConfig> {
    public FullBrightApi() {
        super(FullBright.INSTANCE.getSettings());
    }
}