package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class FastBreak implements Module {

    public static final FastBreak INSTANCE = new FastBreak();

    private FastBreak() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<FastBreakConfig> {

        public WebApi() {
            super("fast-break", FastBreakConfig.class);
        }

        @Override
        protected FastBreakConfig getConfig() {
            return ConfigStore.instance.getConfig().fastBreakConfig;
        }

        @Override
        protected void setConfig(FastBreakConfig config) {
            ConfigStore.instance.getConfig().fastBreakConfig = config;
        }
    }
}