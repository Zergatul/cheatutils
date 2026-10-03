package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.BoatHackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BoatHack implements Module {

    public static final BoatHack INSTANCE = new BoatHack();

    private BoatHack() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<BoatHackConfig> {

        public WebApi() {
            super("boat-hack", BoatHackConfig.class);
        }

        @Override
        protected BoatHackConfig getConfig() {
            return ConfigStore.instance.getConfig().boatHackConfig;
        }

        @Override
        protected void setConfig(BoatHackConfig config) {
            ConfigStore.instance.getConfig().boatHackConfig = config;
        }
    }
}