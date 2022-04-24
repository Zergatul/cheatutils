package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LightLevelConfig;
import com.zergatul.cheatutils.controllers.LightLevelController;
import org.apache.http.MethodNotSupportedException;

public class LightLevelApi extends ApiBase {

    @Override
    public String getRoute() {
        return "light-level";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return gson.toJson(ConfigStore.instance.lightLevelConfig);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        LightLevelConfig config = gson.fromJson(body, LightLevelConfig.class);
        if (config != null) {
            config.maxDistance = Math.max(1, config.maxDistance);
            ConfigStore.instance.lightLevelConfig = config;
            LightLevelController.instance.setActive(config.active);
            ConfigStore.instance.write();
        }
        return gson.toJson(ConfigStore.instance.lightLevelConfig);
    }
}
