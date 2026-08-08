package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CoreConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CoreConfigApi extends SimpleConfigApi<CoreConfig> {

    public CoreConfigApi() {
        super("core", CoreConfig.class);
    }

    @Override
    protected CoreConfig getConfig() {
        return ConfigStore.instance.getConfig().coreConfig;
    }

    @Override
    protected void setConfig(CoreConfig config) {
        ConfigStore.instance.getConfig().coreConfig = config;
        CompletableFuture.delayedExecutor(250, TimeUnit.MILLISECONDS)
                .execute(ConfigHttpServer.instance::onConfigUpdated);
    }
}