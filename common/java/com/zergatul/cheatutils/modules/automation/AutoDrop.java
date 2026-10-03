package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.configs.AutoDropConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class AutoDrop implements Module {

    public static final AutoDrop INSTANCE = new AutoDrop();

    private AutoDrop() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<AutoDropConfig> {

        public WebApi() {
            super("auto-drop", AutoDropConfig.class);
        }

        @Override
        protected AutoDropConfig getConfig() {
            return ConfigStore.instance.getConfig().autoDropConfig;
        }

        @Override
        protected void setConfig(AutoDropConfig config) {
            ConfigStore.instance.getConfig().autoDropConfig = config;
        }
    }
}