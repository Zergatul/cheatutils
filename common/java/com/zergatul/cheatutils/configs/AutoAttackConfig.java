package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoAttackConfig extends ModuleConfig implements Sanitizable {

    public boolean limitRange;
    public double maxRange;
    public int extraTicksMin;
    public int extraTicksMax;

    public AutoAttackConfig() {
        maxRange = 2.5;
        extraTicksMin = 0;
        extraTicksMax = 0;
    }

    @Override
    public void sanitize() {
        maxRange = MathUtils.clamp(maxRange, 0, 10);
        extraTicksMin = MathUtils.clamp(extraTicksMin, -10, 10);
        extraTicksMax = MathUtils.clamp(extraTicksMax, extraTicksMin, 10);
    }
}