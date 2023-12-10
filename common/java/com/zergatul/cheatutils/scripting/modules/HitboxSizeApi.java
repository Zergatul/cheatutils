package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HitboxSizeConfig;

public class HitboxSizeApi extends ModuleApi<HitboxSizeConfig> {

    @Override
    protected HitboxSizeConfig getConfig() {
        return ConfigStore.instance.getConfig().hitboxSizeConfig;
    }
}