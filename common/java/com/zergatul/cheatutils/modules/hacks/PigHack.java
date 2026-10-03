package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PigHackConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class PigHack implements Module {

    public static final PigHack INSTANCE = new PigHack();

    private PigHack() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<PigHackConfig> {

        public WebApi() {
            super("pig-hack", PigHackConfig.class);
        }

        @Override
        protected PigHackConfig getConfig() {
            return ConfigStore.instance.getConfig().pigHackConfig;
        }

        @Override
        protected void setConfig(PigHackConfig config) {
            ConfigStore.instance.getConfig().pigHackConfig = config;
        }
    }
}