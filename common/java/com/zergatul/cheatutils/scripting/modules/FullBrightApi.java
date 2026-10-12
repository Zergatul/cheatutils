package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.FullBrightConfig;
import com.zergatul.cheatutils.modules.visuals.FullBright;

public class FullBrightApi extends SettingHandleApi<FullBrightConfig> {

    public FullBrightApi() {
        super(FullBright.instance.getSettings());
    }
}