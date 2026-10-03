package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CoreConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import com.zergatul.cheatutils.web.WebServer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Core implements Module {

    public static final Core INSTANCE = new Core();

    private Core() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<CoreConfig> {

        public WebApi() {
            super("core", CoreConfig.class);
        }

        @Override
        protected CoreConfig getConfig() {
            return ConfigStore.instance.getConfig().coreConfig;
        }

        @Override
        protected void setConfig(CoreConfig config) {
            ConfigStore.instance.getConfig().coreConfig = config;
            CompletableFuture.delayedExecutor(250, TimeUnit.MILLISECONDS).execute(() -> {
                WebServer.instance.onConfigUpdated();
            });
        }
    }
}