package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ClassRemapper;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;

public class EntitiesApi {

    public boolean isEnabled(String className) {
        var config = getConfig(className);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

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
        synchronized (list) {
            return list.stream().filter(c -> c.clazz.getName().equals(ClassRemapper.toObf(className))).findFirst().orElse(null);
        }
    }
}