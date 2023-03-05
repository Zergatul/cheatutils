package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class EntitiesApi {

    public boolean isEnabled(String className) {
        var config = getConfig(className);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String className) {
        var config = getConfig(className);
        if (config == null) {
            return;
        }
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private EntityTracerConfig getConfig(String className) {
        var list = ConfigStore.instance.getConfig().entities.configs;
        return list.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
    }
}