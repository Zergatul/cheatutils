package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class PerformanceConfig implements Sanitizable {

    public boolean limitBackgroundWindowFps;
    public int backgroundWindowFps;

    public PerformanceConfig() {
        backgroundWindowFps = 20;
    }

    @Override
    public void sanitize() {
        backgroundWindowFps = MathUtils.clamp(backgroundWindowFps, 1, 120);
    }
}