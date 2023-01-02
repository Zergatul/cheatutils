package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ReachConfig implements ValidatableConfig {

    public boolean overrideReachDistance;
    public double reachDistance;
    public boolean overrideAttackRange;
    public double attackRange;

    public ReachConfig() {
        reachDistance = 4.5;
        attackRange = 3.0;
    }

    @Override
    public void validate() {
        reachDistance = MathUtils.clamp(reachDistance, 0, 100);
        attackRange = MathUtils.clamp(attackRange, 0, 100);
    }
}