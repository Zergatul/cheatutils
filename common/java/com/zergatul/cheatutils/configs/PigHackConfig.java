package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class PigHackConfig extends ModuleConfig implements Sanitizable {

    public boolean allowRideWithoutCarrot;
    public boolean overrideSteeringSpeed;
    public float steeringSpeed;

    public PigHackConfig() {
        enabled = false;
        allowRideWithoutCarrot = true;
        overrideSteeringSpeed = true;
        steeringSpeed = 0.1f;
    }

    @Override
    public void sanitize() {
        steeringSpeed = MathUtils.clamp(steeringSpeed, 0.01f, 5f);
    }
}