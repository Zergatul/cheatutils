package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ElytraTunnelConfig extends ModuleConfig implements Sanitizable {

    public double limit;

    public ElytraTunnelConfig() {
        enabled = false;
        limit = 122.8;
    }

    @Override
    public void sanitize() {
        limit = MathUtils.clamp(limit, -1000, 1000);
    }
}