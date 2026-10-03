package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ReachConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Reach implements Module {

    public static final Reach INSTANCE = new Reach();

    private Reach() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<ReachConfig> {

        public WebApi() {
            super("reach", ReachConfig.class);
        }

        @Override
        protected ReachConfig getConfig() {
            return ConfigStore.instance.getConfig().reachConfig;
        }

        @Override
        protected void setConfig(ReachConfig config) {
            ConfigStore.instance.getConfig().reachConfig = config;
        }
    }
}