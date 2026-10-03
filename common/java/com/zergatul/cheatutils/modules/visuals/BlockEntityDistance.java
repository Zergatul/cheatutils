package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.BlockEntityDistanceConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BlockEntityDistance implements Module {

    public static final BlockEntityDistance INSTANCE = new BlockEntityDistance();
    public static int VIEW_DISTANCE_CACHED = 64;

    private BlockEntityDistance() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<BlockEntityDistanceConfig> {

        public WebApi() {
            super("block-entity-distance", BlockEntityDistanceConfig.class);
        }

        @Override
        protected BlockEntityDistanceConfig getConfig() {
            return ConfigStore.instance.getConfig().blockEntityDistance;
        }

        @Override
        protected void setConfig(BlockEntityDistanceConfig config) {
            ConfigStore.instance.getConfig().blockEntityDistance = config;
        }
    }
}