package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoAttackConfig extends ModuleConfig implements Sanitizable {
    public double extraTicks;

    @Override
    public void sanitize() {
        extraTicks = MathUtils.clamp( extraTicks, -10, 10);
    }
}