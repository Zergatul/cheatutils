package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoAttackConfig extends ModuleConfig implements ValidatableConfig {
    public double extraTicks;

    @Override
    public void validate() {
        extraTicks = MathUtils.clamp( extraTicks, -10, 10);
    }
}