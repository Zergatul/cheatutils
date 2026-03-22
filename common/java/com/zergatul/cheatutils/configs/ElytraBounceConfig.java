package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ElytraBounceConfig implements ValidatableConfig {

    public int firstJumpTicks;
    public int betweenJumpsTicks;
    public int secondJumpTicks;

    public ElytraBounceConfig() {
        firstJumpTicks = 1;
        betweenJumpsTicks = 1;
        secondJumpTicks = 1;
    }

    @Override
    public void validate() {
        firstJumpTicks = MathUtils.clamp(firstJumpTicks, 1, 20);
        betweenJumpsTicks = MathUtils.clamp(betweenJumpsTicks, 1, 20);
        secondJumpTicks = MathUtils.clamp(secondJumpTicks, 1, 20);
    }
}