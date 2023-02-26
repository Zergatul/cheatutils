package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.MovementHackConfig;

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

    public boolean isOverrideJumpHeightEnabled() {
        var config = getConfig();
        return config.scaleJumpHeight;
    }

    public String getJumpFactor() {
        var config = getConfig();
        return String.format(Locale.ROOT, "%.3f", config.jumpHeightFactor);
    }

    private MovementHackConfig getConfig() {
        return ConfigStore.instance.getConfig().movementHackConfig;
    }
}