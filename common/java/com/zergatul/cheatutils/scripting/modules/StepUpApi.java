package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.StepUpConfig;
import com.zergatul.cheatutils.modules.hacks.StepUp;

public class StepUpApi extends SettingHandle2Api<StepUpConfig> {
    public StepUpApi() {
        super(StepUp.INSTANCE.getSettings());
    }
}