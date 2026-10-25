package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.ChunksConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class Chunks implements Module {

    public static final Chunks INSTANCE = new Chunks();

    private final SettingHandle2<ChunksConfig> settings = Settings.get(ChunksConfig.class);

    private Chunks() {
        WebApiRegistry.INSTANCE.register("chunks", settings, ChunksConfig.class);
    }

    public SettingHandle2<ChunksConfig> getSettings() {
        return settings;
    }
}