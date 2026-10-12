package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.chunkoverlays.NewChunksOverlay;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.configs.NewChunksConfig;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class NewChunks implements Module {

    public static final NewChunks INSTANCE = new NewChunks();

    private final ModuleConfigSettingHandle<NewChunksConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().newChunksConfig,
            config -> ConfigStore.instance.getConfig().newChunksConfig = config,
            this::onConfigChanged);

    private NewChunks() {
        WebApiRegistry.INSTANCE.register("new-chunks", settings, NewChunksConfig.class);
    }

    public ModuleConfigSettingHandle<NewChunksConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(NewChunksConfig prev, NewChunksConfig curr) {
        if (prev.enabled != curr.enabled) {
            ChunkOverlayController.instance.ofType(NewChunksOverlay.class).onEnabledChanged();
        }
    }
}