package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ParkourAssistConfig implements ValidatableConfig {

    public double threshold;

    public ParkourAssistConfig() {
        threshold = 0.25;
    }

    @Override
    public void validate() {
        threshold = MathUtils.clamp(threshold, 0, 100);
    }
}