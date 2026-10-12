package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CoreConfig;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import com.zergatul.cheatutils.web.WebServer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Core implements Module {

    public static final Core INSTANCE = new Core();

    private final SettingHandle<CoreConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().coreConfig,
            config -> ConfigStore.instance.getConfig().coreConfig = config,
            this::onConfigChanged);

    private Core() {
        WebApiRegistry.INSTANCE.register("core", settings, CoreConfig.class);
    }

    public SettingHandle<CoreConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(CoreConfig prev, CoreConfig curr) {
        if (prev.port != curr.port) {
            CompletableFuture.delayedExecutor(250, TimeUnit.MILLISECONDS).execute(() -> {
                WebServer.instance.onConfigUpdated();
            });
        }
    }
}