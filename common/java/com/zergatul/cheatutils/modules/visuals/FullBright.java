package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FullBrightConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FullBright implements Module {

    public static final FullBright instance = new FullBright();

    private FullBright() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    public boolean shouldFakeNighVision() {
        return ConfigStore.instance.getConfig().fullBrightConfig.enabled;
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<FullBrightConfig> {

        public WebApi() {
            super("full-bright", FullBrightConfig.class);
        }

        @Override
        protected FullBrightConfig getConfig() {
            return ConfigStore.instance.getConfig().fullBrightConfig;
        }

        @Override
        protected void setConfig(FullBrightConfig config) {
            ConfigStore.instance.getConfig().fullBrightConfig = config;
        }
    }
}