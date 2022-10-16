package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EntityTracerConfig;

import java.util.List;

public class EntitiesApi {

    public boolean isEnabled(String className) {
        EntityTracerConfig config = getConfig(className);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    public void toggle(String className) {
        EntityTracerConfig config = getConfig(className);
        if (config == null) {
            return;
        }
        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private EntityTracerConfig getConfig(String className) {
        List<EntityTracerConfig> list = ConfigStore.instance.getConfig().entities.configs;
        synchronized (list) {
            return list.stream().filter(c -> c.clazz.getName().equals(className)).findFirst().orElse(null);
        }
    }
}