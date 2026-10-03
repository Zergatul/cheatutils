package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HandsViewConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class HandsView implements Module {

    public static final HandsView INSTANCE = new HandsView();

    private HandsView() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<HandsViewConfig> {

        public WebApi() {
            super("hands-view", HandsViewConfig.class);
        }

        @Override
        protected HandsViewConfig getConfig() {
            return ConfigStore.instance.getConfig().handsViewConfig;
        }

        @Override
        protected void setConfig(HandsViewConfig config) {
            ConfigStore.instance.getConfig().handsViewConfig = config;
        }
    }
}