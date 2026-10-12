package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.ChunksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Chunks implements Module {

    public static final Chunks INSTANCE = new Chunks();

    private final SettingHandle<ChunksConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().chunksConfig,
            config -> ConfigStore.instance.getConfig().chunksConfig = config);

    private Chunks() {
        WebApiRegistry.INSTANCE.register("chunks", settings, ChunksConfig.class);
    }

    public SettingHandle<ChunksConfig> getSettings() {
        return settings;
    }
}