package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class ContainerButtons implements Module {

    public static final ContainerButtons INSTANCE = new ContainerButtons();

    private ContainerButtons() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<ContainerButtonsConfig> {

        public WebApi() {
            super("container-buttons", ContainerButtonsConfig.class);
        }

        @Override
        protected ContainerButtonsConfig getConfig() {
            return ConfigStore.instance.getConfig().containerButtonsConfig;
        }

        @Override
        protected void setConfig(ContainerButtonsConfig config) {
            ContainerButtonsConfig oldConfig = ConfigStore.instance.getConfig().containerButtonsConfig;
            if (!oldConfig.autoDropAll && config.autoDropAll) {
                config.autoTakeAll = false;
            }
            if (!oldConfig.autoTakeAll && config.autoTakeAll) {
                config.autoDropAll = false;
            }
            ConfigStore.instance.getConfig().containerButtonsConfig = config;
        }
    }
}