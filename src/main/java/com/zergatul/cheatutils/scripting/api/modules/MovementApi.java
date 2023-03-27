package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

import java.util.Locale;

public class MovementApi {

    public boolean isSpeedMultiplierEnabled() {
        var config = getConfig();
        return config.scaleInputVector;
    }

    public String getSpeedMultiplierFactor() {
        var config = getConfig();
        return String.format(Locale.ROOT, "%.3f", config.inputVectorFactor);
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
        config.validate();
        ConfigStore.instance.requestWrite();
    }

    private MovementHackConfig getConfig() {
        return ConfigStore.instance.getConfig().movementHackConfig;
    }
}