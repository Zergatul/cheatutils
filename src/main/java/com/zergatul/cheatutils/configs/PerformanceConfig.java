package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class PerformanceConfig implements ValidatableConfig {

    public boolean limitBackgroundWindowFps;
    public int backgroundWindowFps;

    public PerformanceConfig() {
        backgroundWindowFps = 20;
    }

    @Override
    public void validate() {
        backgroundWindowFps = MathUtils.clamp(backgroundWindowFps, 1, 120);
    }
}