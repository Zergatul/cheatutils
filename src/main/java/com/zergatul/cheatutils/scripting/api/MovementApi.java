package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;

public class MovementApi {

    public boolean isSpeedMultiplierEnabled() {
        var config = getConfig();
        return config.scaleInputVector;
    }

    public void toggleSpeedMultiplier() {
        var config = getConfig();
        config.scaleInputVector = !config.scaleInputVector;
        ConfigStore.instance.requestWrite();
    }

    public void setSpeedMultiplierFactor(double value) {
        var config = getConfig();
        config.inputVectorFactor = value;
        config.validate();
        ConfigStore.instance.requestWrite();
    }

    private MovementHackConfig getConfig() {
        return ConfigStore.instance.getConfig().movementHackConfig;
    }
}