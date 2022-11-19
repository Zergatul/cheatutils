package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ElytraHackConfig implements ValidatableConfig {

    public boolean enabled;
    public boolean heightControl;
    public boolean speedControl;
    public boolean horizontalSpeedLimitEnabled;
    public double horizontalSpeedLimit;
    public boolean speedLimitEnabled;
    public double speedLimit;

    public ElytraHackConfig() {
        enabled = false;
        heightControl = false;
        speedControl = false;
        horizontalSpeedLimitEnabled = false;
        horizontalSpeedLimit = 100;
        speedLimitEnabled = false;
        speedLimit = 100;
    }

    @Override
    public void validate() {
        horizontalSpeedLimit = MathUtils.clamp(horizontalSpeedLimit, 10, 1000);
        speedLimit = MathUtils.clamp(speedLimit, 10, 1000);
    }
}