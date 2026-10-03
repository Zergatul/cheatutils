package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Movement implements Module {

    public static final Movement INSTANCE = new Movement();

    private Movement() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<MovementHackConfig> {

        public WebApi() {
            super("movement-hack", MovementHackConfig.class);
        }

        @Override
        protected MovementHackConfig getConfig() {
            return ConfigStore.instance.getConfig().movementHackConfig;
        }

        @Override
        protected void setConfig(MovementHackConfig config) {
            ConfigStore.instance.getConfig().movementHackConfig = config;
        }
    }
}