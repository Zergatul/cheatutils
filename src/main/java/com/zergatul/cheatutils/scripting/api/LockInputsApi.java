package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;

public class LockInputsApi {

    public void toggleHoldForward() {
        var config = getConfig();
        config.holdForward = !config.holdForward;
        ConfigStore.instance.requestWrite();
    }

    public void toggleHoldUse() {
        var config = getConfig();
        config.holdUse = !config.holdUse;
        ConfigStore.instance.requestWrite();
    }

    public void toggleMouseInputDisabled() {
        var config = getConfig();
        config.mouseInputDisabled = !config.mouseInputDisabled;
        ConfigStore.instance.requestWrite();
    }

    private LockInputsConfig getConfig() {
        return ConfigStore.instance.getConfig().lockInputsConfig;
    }
}