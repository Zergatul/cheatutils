package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class FastBreakConfig implements ValidatableConfig {
    public boolean enabled;
    public double factor;

    public void validate() {
        factor = MathUtils.clamp(factor, 1, 10);
    }
}