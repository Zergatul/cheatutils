package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoEatConfig extends ModuleConfig implements ValidatableConfig {

    public boolean isHungerLimitEnabled;
    public int hungerLimit;

    public AutoEatConfig() {
        hungerLimit = 10;
    }

    @Override
    public void validate() {
        hungerLimit = MathUtils.clamp(hungerLimit, 0, 100);
    }
}