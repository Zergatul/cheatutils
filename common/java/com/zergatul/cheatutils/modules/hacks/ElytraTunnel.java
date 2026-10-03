package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraTunnelConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class ElytraTunnel implements Module {

    public static final ElytraTunnel INSTANCE = new ElytraTunnel();

    private ElytraTunnel() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<ElytraTunnelConfig> {

        public WebApi() {
            super("elytra-tunnel", ElytraTunnelConfig.class);
        }

        @Override
        protected ElytraTunnelConfig getConfig() {
            return ConfigStore.instance.getConfig().elytraTunnelConfig;
        }

        @Override
        protected void setConfig(ElytraTunnelConfig config) {
            ConfigStore.instance.getConfig().elytraTunnelConfig = config;
        }
    }
}