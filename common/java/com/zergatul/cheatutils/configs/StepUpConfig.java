package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class StepUpConfig extends ModuleConfig implements ValidatableConfig {

    public double height;

    public StepUpConfig() {
        height = 1;
    }

    @Override
    public void validate() {
        height = MathUtils.clamp(height, 0, 100);
    }
}