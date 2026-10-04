package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ProjectilePathConfig;

public class ProjectilePathApi extends ModuleApi<ProjectilePathConfig> {

    @Override
    protected ProjectilePathConfig getConfig() {
        return ConfigStore.instance.getConfig().projectilePathConfig;
    }
}