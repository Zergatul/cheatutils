package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ProjectilePathConfig;
import com.zergatul.cheatutils.modules.esp.ProjectilePath;

public class ProjectilePathApi extends SettingHandleApi<ProjectilePathConfig> {

    public ProjectilePathApi() {
        super(ProjectilePath.instance.getSettings());
    }
}