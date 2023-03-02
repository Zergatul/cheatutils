package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;

public class ContainerButtonsApi {

    public void toggleAutoTakeAll() {
        ContainerButtonsConfig config = getConfig();
        config.autoTakeAll = !config.autoTakeAll;
        ConfigStore.instance.requestWrite();
    }

    public void toggleAutoDropAll() {
        ContainerButtonsConfig config = getConfig();
        config.autoDropAll = !config.autoDropAll;
        ConfigStore.instance.requestWrite();
    }

    public void toggleAutoClose() {
        ContainerButtonsConfig config = getConfig();
        config.autoClose = !config.autoClose;
        ConfigStore.instance.requestWrite();
    }

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