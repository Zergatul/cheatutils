package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.StepUpConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class StepUp implements Module {

    public static final StepUp INSTANCE = new StepUp();

    private StepUp() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<StepUpConfig> {

        public WebApi() {
            super("step-up", StepUpConfig.class);
        }

        @Override
        protected StepUpConfig getConfig() {
            return ConfigStore.instance.getConfig().stepUp;
        }

        @Override
        protected void setConfig(StepUpConfig config) {
            ConfigStore.instance.getConfig().stepUp = config;
        }
    }
}