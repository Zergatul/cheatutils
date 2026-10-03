package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class LockInputs {

    public static final LockInputs instance = new LockInputs();

    private LockInputs() {
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);

        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (ConfigStore.instance.getConfig().lockInputsConfig.mouseInputDisabled) {
            event.cancel();
        }
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<LockInputsConfig> {

        public WebApi() {
            super("lock-inputs", LockInputsConfig.class);
        }

        @Override
        protected LockInputsConfig getConfig() {
            return ConfigStore.instance.getConfig().lockInputsConfig;
        }

        @Override
        protected void setConfig(LockInputsConfig config) {
            ConfigStore.instance.getConfig().lockInputsConfig = config;
        }
    }
}