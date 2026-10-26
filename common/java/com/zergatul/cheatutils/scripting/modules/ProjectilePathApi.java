package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.ProjectilePathConfig;
import com.zergatul.cheatutils.modules.esp.ProjectilePath;

public class ProjectilePathApi extends SettingHandle2Api<ProjectilePathConfig> {
    public ProjectilePathApi() {
        super(ProjectilePath.INSTANCE.getSettings());
    }
}