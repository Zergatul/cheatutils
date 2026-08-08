package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ReachConfig implements Sanitizable, ModuleStateProvider {

    public boolean overrideReachDistance;
    public double reachDistance;
    public boolean overrideAttackRange;
    public double attackRange;

    public ReachConfig() {
        reachDistance = 4.5;
        attackRange = 3.0;
    }

    @Override
    public void sanitize() {
        reachDistance = MathUtils.clamp(reachDistance, 0, 100);
        attackRange = MathUtils.clamp(attackRange, 0, 100);
    }

    @Override
    public boolean isEnabled() {
        return overrideAttackRange || overrideReachDistance;
    }
}