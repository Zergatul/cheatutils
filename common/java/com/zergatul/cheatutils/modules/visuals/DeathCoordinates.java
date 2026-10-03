package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.DeathCoordinatesConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class DeathCoordinates implements Module {

    public static final DeathCoordinates INSTANCE = new DeathCoordinates();

    private DeathCoordinates() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<DeathCoordinatesConfig> {

        public WebApi() {
            super("death-coordinates", DeathCoordinatesConfig.class);
        }

        @Override
        protected DeathCoordinatesConfig getConfig() {
            return ConfigStore.instance.getConfig().deathCoordinatesConfig;
        }

        @Override
        protected void setConfig(DeathCoordinatesConfig config) {
            ConfigStore.instance.getConfig().deathCoordinatesConfig = config;
        }
    }
}