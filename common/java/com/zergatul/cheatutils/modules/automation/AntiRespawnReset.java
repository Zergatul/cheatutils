package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.AntiRespawnResetConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AntiRespawnReset implements Module {

    public static final AntiRespawnReset INSTANCE = new AntiRespawnReset();

    private AntiRespawnReset() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<AntiRespawnResetConfig> {

        public WebApi() {
            super("anti-respawn-reset", AntiRespawnResetConfig.class);
        }

        @Override
        protected AntiRespawnResetConfig getConfig() {
            return ConfigStore.instance.getConfig().antiRespawnResetConfig;
        }

        @Override
        protected void setConfig(AntiRespawnResetConfig config) {
            ConfigStore.instance.getConfig().antiRespawnResetConfig = config;
        }
    }
}
