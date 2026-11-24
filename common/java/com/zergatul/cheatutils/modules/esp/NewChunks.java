package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.chunkoverlays.NewChunksOverlay;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.NewChunksConfig;
import com.zergatul.cheatutils.controllers.ChunkOverlayController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class NewChunks implements Module {

    public static final NewChunks INSTANCE = new NewChunks();

    private final SettingHandle<NewChunksConfig> settings = Settings.get(NewChunksConfig.class);

    private NewChunks() {
        settings.onChanged(this::onConfigChanged);

        WebApiRegistry.INSTANCE.register("new-chunks", settings, NewChunksConfig.class);
    }

    public SettingHandle<NewChunksConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(NewChunksConfig prev, NewChunksConfig curr) {
        if (prev.enabled() != curr.enabled()) {
            ChunkOverlayController.INSTANCE.ofType(NewChunksOverlay.class).onEnabledChanged();
        }
    }
}