package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class FastBreakConfig {
    public boolean enabled;
    public double factor;
    public boolean disableFlyPenalty;

    public FastBreakConfig() {
        factor = 1.2;
    }

    public void validate() {
        factor = MathUtils.clamp(factor, 0.5, 10);
    }
}