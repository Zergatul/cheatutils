package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

import java.util.HashMap;
import java.util.Map;

public class ModulesStatusWebApi extends WebApiBase {

    public static final ModulesStatusWebApi INSTANCE = new ModulesStatusWebApi();

    private ModulesStatusWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

    @Override
    public String getRoute() {
        return "modules-status";
    }

    @Override
    public String get() {
        Map<Class<? extends ImmutableConfig>, Boolean> states = Settings.getModuleStates();

        Map<String, Boolean> map = new HashMap<>();
        for (var entry : states.entrySet()) {
            Class<? extends ImmutableConfig> type = entry.getKey();
            boolean enabled = entry.getValue();
            String key = type.getSimpleName();
            if (key.endsWith("Config")) {
                key = key.substring(0, key.length() - 6);
            }
            map.put(key, enabled);
        }

        return gson.toJson(map);
    }
}