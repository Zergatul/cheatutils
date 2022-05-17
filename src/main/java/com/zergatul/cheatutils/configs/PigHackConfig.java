package com.zergatul.cheatutils.configs;

public class PigHackConfig {

    public boolean enabled;
    public boolean allowRideWithoutCarrot;
    public boolean overrideSteeringSpeed;
    public float steeringSpeed;

    public PigHackConfig() {
        enabled = false;
        allowRideWithoutCarrot = true;
        overrideSteeringSpeed = true;
        steeringSpeed = 0.1f;
    }
}
