package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;

public class MovementApi {

    public boolean isSpeedMultiplierEnabled() {
        var config = getConfig();
        return config.scaleInputVector;
    }

    public double getSpeedMultiplierFactor() {
        return getConfig().inputVectorFactor;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleSpeedMultiplier() {
        var config = getConfig();
        config.scaleInputVector = !config.scaleInputVector;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setSpeedMultiplierFactor(double value) {
        var config = getConfig();
        config.inputVectorFactor = value;
        config.sanitize();
        ConfigStore.instance.requestWrite();
    }

    public boolean isOverrideJumpHeightEnabled() {
        var config = getConfig();
        return config.scaleJumpHeight;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleOverrideJumpHeight() {
        var config = getConfig();
        config.scaleJumpHeight = !config.scaleJumpHeight;
        ConfigStore.instance.requestWrite();
    }

    public double getJumpFactor() {
        return getConfig().jumpHeightFactor;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setJumpFactor(double value) {
        var config = getConfig();
        config.jumpHeightFactor = value;
        config.sanitize();
        ConfigStore.instance.requestWrite();
    }

    private MovementHackConfig getConfig() {
        return ConfigStore.instance.getConfig().movementHackConfig;
    }
}