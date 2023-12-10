package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;

public class LockInputsApi {

    public boolean isHoldForwardEnabled() {
        return getConfig().holdForward;
    }

    public boolean isHoldUseEnabled() {
        return getConfig().holdUse;
    }

    public boolean isHoldAttackEnabled() {
        return getConfig().holdAttack;
    }

    public boolean isMouseInputDisabled() {
        return getConfig().mouseInputDisabled;
    }

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
    public void toggleHoldAttack() {
        var config = getConfig();
        config.holdAttack = !config.holdAttack;
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