package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class StepUpConfig extends ModuleConfig implements Sanitizable {

    public double height;

    public StepUpConfig() {
        height = 1;
    }

    @Override
    public void sanitize() {
        height = MathUtils.clamp(height, 0, 100);
    }
}