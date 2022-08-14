package com.zergatul.cheatutils.configs;

public class ElytraHackConfig {

    public boolean enabled;
    public boolean heightControl;
    public boolean speedControl;
    public boolean horizontalFlight;
    public boolean horizontalSpeedLimitEnabled;
    public double horizontalSpeedLimit;

    public ElytraHackConfig() {
        enabled = false;
        heightControl = false;
        speedControl = false;
        horizontalFlight = false;
        horizontalSpeedLimitEnabled = false;
        horizontalSpeedLimit = 100;
    }
}
