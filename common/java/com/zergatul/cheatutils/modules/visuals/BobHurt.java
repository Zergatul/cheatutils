package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.BobHurtConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BobHurt implements Module {

    public static final BobHurt INSTANCE = new BobHurt();

    private BobHurt() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<BobHurtConfig> {

        public WebApi() {
            super("bob-hurt", BobHurtConfig.class);
        }

        @Override
        protected BobHurtConfig getConfig() {
            return ConfigStore.instance.getConfig().bobHurtConfig;
        }

        @Override
        protected void setConfig(BobHurtConfig config) {
            ConfigStore.instance.getConfig().bobHurtConfig = config;
        }
    }
}