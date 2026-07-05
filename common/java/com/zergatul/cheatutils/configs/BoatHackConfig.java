package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class BoatHackConfig implements Sanitizable, ModuleStateProvider {

    public boolean fly;
    public double horizontalFlySpeed;
    public double verticalFlySpeed;

    public BoatHackConfig() {
        horizontalFlySpeed = 10;
        verticalFlySpeed = 5;
    }

    @Override
    public void sanitize() {
        horizontalFlySpeed = horizontalFlySpeed;
        verticalFlySpeed = verticalFlySpeed;
    }

    @Override
    public boolean isEnabled() {
        return fly;
    }
}