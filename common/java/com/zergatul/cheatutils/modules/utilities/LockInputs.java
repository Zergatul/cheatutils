package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class LockInputs {

    public static final LockInputs instance = new LockInputs();

    private final SettingHandle<LockInputsConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().lockInputsConfig,
            config -> ConfigStore.instance.getConfig().lockInputsConfig = config);

    private LockInputs() {
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);

        WebApiRegistry.INSTANCE.register("lock-inputs", settings, LockInputsConfig.class);
    }

    public SettingHandle<LockInputsConfig> getSettings() {
        return settings;
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (settings.get().mouseInputDisabled) {
            event.cancel();
        }
    }
}