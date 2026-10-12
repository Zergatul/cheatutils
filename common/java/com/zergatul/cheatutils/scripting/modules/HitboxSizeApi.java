package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.HitboxSizeConfig;
import com.zergatul.cheatutils.modules.hacks.HitboxSize;

public class HitboxSizeApi extends SettingHandleApi<HitboxSizeConfig> {

    public HitboxSizeApi() {
        super(HitboxSize.instance.getSettings());
    }
}