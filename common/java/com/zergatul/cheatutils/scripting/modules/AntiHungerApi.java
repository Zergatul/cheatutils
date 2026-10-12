package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AntiHungerConfig;
import com.zergatul.cheatutils.modules.hacks.AntiHunger;

public class AntiHungerApi extends SettingHandleApi<AntiHungerConfig> {
    public AntiHungerApi() {
        super(AntiHunger.INSTANCE.getSettings());
    }
}