package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.configs.ConfigStore;

public class LockInputs {

    public static final LockInputs instance = new LockInputs();

    private LockInputs() {
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (ConfigStore.instance.getConfig().lockInputsConfig.mouseInputDisabled) {
            event.cancel();
        }
    }
}