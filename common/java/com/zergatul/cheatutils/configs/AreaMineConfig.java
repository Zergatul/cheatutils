package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AreaMineConfig extends ModuleConfig implements Sanitizable {

    public double radius;
    public boolean preview;

    public AreaMineConfig() {
        radius = 3;
    }

    @Override
    public void sanitize() {
        radius = MathUtils.clamp(radius, 1, 20);
    }
}
