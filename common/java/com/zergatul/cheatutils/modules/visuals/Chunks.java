package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ChunksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Chunks implements Module {

    public static final Chunks INSTANCE = new Chunks();

    private Chunks() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<ChunksConfig> {

        public WebApi() {
            super("chunks", ChunksConfig.class);
        }

        @Override
        protected ChunksConfig getConfig() {
            return ConfigStore.instance.getConfig().chunksConfig;
        }

        @Override
        protected void setConfig(ChunksConfig config) {
            ConfigStore.instance.getConfig().chunksConfig = config;
        }
    }
}