package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;

import java.util.function.Consumer;

public class MovementApi {

    public boolean isSpeedMultiplierEnabled() {
        var config = getConfig();
        return config.scaleInputVector;
    }

    public double getSpeedMultiplierFactor() {
        var config = getConfig();
        return config.inputVectorFactor;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleSpeedMultiplier() {
        update(config -> config.scaleInputVector = !config.scaleInputVector);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setSpeedMultiplierFactor(double value) {
        update(config -> config.inputVectorFactor = value);
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
        var config = getConfig();
        return config.jumpHeightFactor;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setJumpFactor(double value) {
        update(config -> config.jumpHeightFactor = value);
    }

    private void update(Consumer<MovementHackConfig> update) {
        ConfigStore.updateFromApi(config -> config.movementHackConfig, update);
    }

    private MovementHackConfig getConfig() {
        return ConfigStore.instance.getConfig().movementHackConfig;
    }
}