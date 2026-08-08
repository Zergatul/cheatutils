package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class PerformanceConfig implements Sanitizable, ModuleStateProvider {

    public boolean limitBackgroundWindowFps;
    public int backgroundWindowFps;

    public PerformanceConfig() {
        backgroundWindowFps = 20;
    }

    @Override
    public void sanitize() {
        backgroundWindowFps = MathUtils.clamp(backgroundWindowFps, 1, 120);
    }

    @Override
    public boolean isEnabled() {
        return limitBackgroundWindowFps;
    }
}