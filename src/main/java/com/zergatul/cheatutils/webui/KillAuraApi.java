package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.JsonKillAuraConfig;
import com.zergatul.cheatutils.configs.LightLevelConfig;
import com.zergatul.cheatutils.controllers.LightLevelController;
import org.apache.http.MethodNotSupportedException;

public class KillAuraApi extends ApiBase {

    @Override
    public String getRoute() {
        return "kill-aura";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return gson.toJson(ConfigStore.instance.killAuraConfig.convert());
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        var jsonConfig = gson.fromJson(body, JsonKillAuraConfig.class);
        if (jsonConfig != null) {
            var config = jsonConfig.convert();
            config.maxRange = Math.max(1, config.maxRange);

            ConfigStore.instance.killAuraConfig = config;
            LightLevelController.instance.setActive(config.active);
            ConfigStore.instance.write();
        }
        return gson.toJson(ConfigStore.instance.killAuraConfig.convert());
    }
}
