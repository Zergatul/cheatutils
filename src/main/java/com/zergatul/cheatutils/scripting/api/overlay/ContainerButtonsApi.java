package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;

public class ContainerButtonsApi {

    public boolean isAutoTakeAllEnabled() {
        return getConfig().autoTakeAll;
    }

    public boolean isAutoDropAllEnabled() {
        return getConfig().autoDropAll;
    }

    public boolean isAutoCloseEnabled() {
        return getConfig().autoClose;
    }

    private ContainerButtonsConfig getConfig() {
        return ConfigStore.instance.getConfig().containerButtonsConfig;
    }
}