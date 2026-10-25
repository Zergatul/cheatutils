package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.ScaffoldConfig;
import com.zergatul.cheatutils.modules.hacks.Scaffold;

public class ScaffoldApi extends SettingHandle2Api<ScaffoldConfig> {
    public ScaffoldApi() {
        super(Scaffold.INSTANCE.getSettings());
    }
}