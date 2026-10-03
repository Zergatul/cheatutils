package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PerformanceConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Performance implements Module {

    public static final Performance INSTANCE = new Performance();

    private Performance() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<PerformanceConfig> {

        public WebApi() {
            super("performance", PerformanceConfig.class);
        }

        @Override
        protected PerformanceConfig getConfig() {
            return ConfigStore.instance.getConfig().performanceConfig;
        }

        @Override
        protected void setConfig(PerformanceConfig config) {
            ConfigStore.instance.getConfig().performanceConfig = config;
        }
    }
}