package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfig;
import org.apache.http.HttpException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ModulesStatusApi extends ApiBase {

    @Override
    public String getRoute() {
        return "modules-status";
    }

    @Override
    public String get() throws HttpException {
        Config config = ConfigStore.instance.getConfig();

        Map<String, Boolean> map = new HashMap<>();
        for (Field field : Config.class.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            if (ModuleConfig.class.isAssignableFrom(fieldType)) {
                ModuleConfig moduleConfig;
                try {
                    moduleConfig = (ModuleConfig) field.get(config);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }

                String key = fieldType.getSimpleName();
                if (key.endsWith("Config")) {
                    key = key.substring(0, key.length() - 6);
                }

                map.put(key, moduleConfig.enabled);
            }
        }

        return gson.toJson(map);
    }
}