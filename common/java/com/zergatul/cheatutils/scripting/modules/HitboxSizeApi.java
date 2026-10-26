package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.HitboxSizeConfig;
import com.zergatul.cheatutils.modules.hacks.HitboxSize;

public class HitboxSizeApi extends SettingHandle2Api<HitboxSizeConfig> {
    public HitboxSizeApi() {
        super(HitboxSize.INSTANCE.getSettings());
    }
}