package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ScaffoldConfig;
import com.zergatul.cheatutils.modules.hacks.Scaffold;

public class ScaffoldApi extends SettingHandleApi<ScaffoldConfig> {

    public ScaffoldApi() {
        super(Scaffold.instance.getSettings());
    }
}