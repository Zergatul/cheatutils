package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoAttackConfig extends ModuleConfig implements ValidatableConfig {

    public boolean limitRange;
    public double maxRange;
    public double extraTicks;

    public AutoAttackConfig() {
        maxRange = 2.5;
    }

    @Override
    public void validate() {
        maxRange = MathUtils.clamp(maxRange, 0, 10);
        extraTicks = MathUtils.clamp(extraTicks, -10, 10);
    }
}