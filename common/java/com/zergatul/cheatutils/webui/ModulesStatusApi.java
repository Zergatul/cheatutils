package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ModulesStatusApi extends ApiBase {

    @Override
    public String getRoute() {
        return "modules-status";
    }

    @Override
    public String get() {
        Config config = ConfigStore.instance.getConfig();

        Map<String, Boolean> map = new HashMap<>();
        for (Field field : Config.class.getDeclaredFields()) {
            if (!ModuleStateProvider.class.isAssignableFrom(field.getType())) {
                continue;
            }

            ModuleStateProvider provider;
            try {
                provider = (ModuleStateProvider) field.get(config);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (provider == null) {
                continue;
            }

            String key = field.getType().getSimpleName();
            if (key.endsWith("Config")) {
                key = key.substring(0, key.length() - 6);
            }
            if (key.equals("Blocks")) {
                key = "BlockESP";
            } else if (key.equals("Entities")) {
                key = "EntityESP";
            }

            map.put(key, provider.isEnabled());
        }

        return gson.toJson(map);
    }
}