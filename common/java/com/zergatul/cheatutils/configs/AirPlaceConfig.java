package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AirPlaceConfig implements ValidatableConfig {

    public double minRange;
    public double maxRange;

    public AirPlaceConfig() {
        minRange = 1;
        maxRange = 5;
    }

    @Override
    public void validate() {
        minRange = MathUtils.clamp(minRange, 0.5, 5);
        maxRange = MathUtils.clamp(maxRange, minRange, 10);
    }
}