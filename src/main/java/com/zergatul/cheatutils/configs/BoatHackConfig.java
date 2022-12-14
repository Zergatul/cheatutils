package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class BoatHackConfig implements ValidatableConfig {
    public boolean overrideFriction;
    public float friction;
    //public boolean disableInertia;
    public boolean fly;
    public double horizontalFlySpeed;
    public double verticalFlySpeed;

    public BoatHackConfig() {
        overrideFriction = false;
        friction = 0.9f;
        horizontalFlySpeed = 10;
        verticalFlySpeed = 5;
    }

    @Override
    public void validate() {
        friction = Math.min(Math.max(0.01f, friction), 0.99f);
        horizontalFlySpeed = MathUtils.clamp(horizontalFlySpeed, 0, 100);
        verticalFlySpeed = MathUtils.clamp(verticalFlySpeed, 0, 100);
    }
}