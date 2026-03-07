package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class MovementApi {

    public double getJumpFactor() {
        return getConfig().jumpHeightFactor;
    }

    public double getSpeedMultiplierFactor() {
        return getConfig().inputVectorFactor;
    }

    public boolean isOverrideJumpHeightEnabled() {
        return getConfig().scaleJumpHeight;
    }

    public boolean isSpeedMultiplierEnabled() {
        return getConfig().scaleInputVector;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setJumpFactor(double value) {
        update(c -> c.jumpHeightFactor = value);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setSpeedMultiplierFactor(double value) {
        update(c -> c.inputVectorFactor = value);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleOverrideJumpHeight() {
        var config = getConfig();
        config.scaleJumpHeight = !config.scaleJumpHeight;
        ConfigStore.instance.requestWrite();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleSpeedMultiplier() {
        update(c -> c.scaleInputVector = !c.scaleInputVector);
    }

    private void update(Consumer<MovementHackConfig> update) {
        ConfigStore.updateFromApi(c -> c.movementHackConfig, update);
    }

    private MovementHackConfig getConfig() {
        return ConfigStore.instance.getConfig().movementHackConfig;
    }
}