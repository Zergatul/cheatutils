package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.StepUpConfig;
import com.zergatul.cheatutils.modules.hacks.StepUp;

public class StepUpApi extends SettingHandleApi<StepUpConfig> {

    public StepUpApi() {
        super(StepUp.INSTANCE.getSettings());
    }
}
