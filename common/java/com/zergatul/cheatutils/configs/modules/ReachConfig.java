package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import com.zergatul.cheatutils.utils.MathUtils;

public record ReachConfig(
        boolean overrideReachDistance,
        double reachDistance,
        boolean overrideAttackRange,
        double attackRange
) implements ImmutableConfig, ModuleStateProvider {

    public ReachConfig {
        reachDistance = MathUtils.clamp(reachDistance, 0, 100);
        attackRange = MathUtils.clamp(attackRange, 0, 100);
    }

    public static ReachConfig defaults() {
        return new Builder()
                .overrideReachDistance(false)
                .reachDistance(4.5)
                .overrideAttackRange(false)
                .attackRange(3.0)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return overrideAttackRange || overrideReachDistance;
    }

    // <generated-code>

    public ReachConfig withOverrideReachDistance(boolean overrideReachDistance) {
        return Builder.from(this).overrideReachDistance(overrideReachDistance).build();
    }

    public ReachConfig withReachDistance(double reachDistance) {
        return Builder.from(this).reachDistance(reachDistance).build();
    }

    public ReachConfig withOverrideAttackRange(boolean overrideAttackRange) {
        return Builder.from(this).overrideAttackRange(overrideAttackRange).build();
    }

    public ReachConfig withAttackRange(double attackRange) {
        return Builder.from(this).attackRange(attackRange).build();
    }

    public static class Builder {

        private boolean overrideReachDistance;
        private double reachDistance;
        private boolean overrideAttackRange;
        private double attackRange;

        private Builder() {}

        public static Builder from(ReachConfig config) {
            return new Builder()
                    .overrideReachDistance(config.overrideReachDistance)
                    .reachDistance(config.reachDistance)
                    .overrideAttackRange(config.overrideAttackRange)
                    .attackRange(config.attackRange);
        }

        public Builder overrideReachDistance(boolean overrideReachDistance) {
            this.overrideReachDistance = overrideReachDistance;
            return this;
        }

        public Builder reachDistance(double reachDistance) {
            this.reachDistance = reachDistance;
            return this;
        }

        public Builder overrideAttackRange(boolean overrideAttackRange) {
            this.overrideAttackRange = overrideAttackRange;
            return this;
        }

        public Builder attackRange(double attackRange) {
            this.attackRange = attackRange;
            return this;
        }

        public ReachConfig build() {
            return new ReachConfig(overrideReachDistance, reachDistance, overrideAttackRange, attackRange);
        }
    }

    // </generated-code>
}
