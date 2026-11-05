package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class TridentHackConfig extends ModuleConfig implements Sanitizable {

    public boolean allowOutOfWater;
    public double boost;

    public TridentHackConfig() {
        boost = 1;
    }

    @Override
    public void sanitize() {
        boost = MathUtils.clamp(boost, 0.1, 1000.0);
    }
}