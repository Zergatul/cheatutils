package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class BoatHackConfig implements ValidatableConfig, ModuleStateProvider {

    public boolean fly;
    public double horizontalFlySpeed;
    public double verticalFlySpeed;

    public BoatHackConfig() {
        horizontalFlySpeed = 10;
        verticalFlySpeed = 5;
    }

    @Override
    public void validate() {
        horizontalFlySpeed = MathUtils.clamp(horizontalFlySpeed, 0, 100);
        verticalFlySpeed = MathUtils.clamp(verticalFlySpeed, 0, 100);
    }

    @Override
    public boolean isEnabled() {
        return fly;
    }
}