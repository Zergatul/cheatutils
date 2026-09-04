package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class FastBreakConfig extends ModuleConfig implements Sanitizable {
    public double factor;
    public boolean disableDestroyDelay;

    public FastBreakConfig() {
        factor = 1.2;
    }

    public void sanitize() {
        factor = MathUtils.clamp(factor, 0.5, 10);
    }
}