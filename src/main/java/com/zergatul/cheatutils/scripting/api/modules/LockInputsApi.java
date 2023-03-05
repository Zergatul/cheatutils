package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

public class LockInputsApi {

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldForward() {
        var config = getConfig();
        config.holdForward = !config.holdForward;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldUse() {
        var config = getConfig();
        config.holdUse = !config.holdUse;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleMouseInputDisabled() {
        var config = getConfig();
        config.mouseInputDisabled = !config.mouseInputDisabled;
        ConfigStore.instance.requestWrite();
    }

    private LockInputsConfig getConfig() {
        return ConfigStore.instance.getConfig().lockInputsConfig;
    }
}