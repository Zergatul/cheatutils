package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.chunkoverlays.NewChunksOverlay;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.NewChunksConfig;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class NewChunks implements Module {

    public static final NewChunks INSTANCE = new NewChunks();

    private NewChunks() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<NewChunksConfig> {

        public WebApi() {
            super("new-chunks", NewChunksConfig.class);
        }

        @Override
        protected NewChunksConfig getConfig() {
            return ConfigStore.instance.getConfig().newChunksConfig;
        }

        @Override
        protected void setConfig(NewChunksConfig config) {
            NewChunksConfig oldConfig = ConfigStore.instance.getConfig().newChunksConfig;
            ConfigStore.instance.getConfig().newChunksConfig = config;

            if (oldConfig.enabled != config.enabled) {
                ChunkOverlayController.instance.ofType(NewChunksOverlay.class).onEnabledChanged();
            }
        }
    }
}