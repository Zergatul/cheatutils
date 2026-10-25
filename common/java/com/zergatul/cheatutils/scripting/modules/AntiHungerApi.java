package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AntiHungerConfig;
import com.zergatul.cheatutils.modules.hacks.AntiHunger;

public class AntiHungerApi extends SettingHandle2Api<AntiHungerConfig> {
    public AntiHungerApi() {
        super(AntiHunger.INSTANCE.getSettings());
    }
}