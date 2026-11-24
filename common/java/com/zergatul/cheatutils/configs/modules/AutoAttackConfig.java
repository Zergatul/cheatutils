package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AutoAttackConfig(
        boolean enabled,
        boolean limitRange,
        double maxRange,
        int extraTicksMin,
        int extraTicksMax
) implements ImmutableConfig, ToggleableConfig<AutoAttackConfig> {

    public AutoAttackConfig {
        maxRange = MathUtils.clamp(maxRange, 0, 10);
        extraTicksMin = MathUtils.clamp(extraTicksMin, -10, 10);
        extraTicksMax = MathUtils.clamp(extraTicksMax, extraTicksMin, 10);
    }

    public static AutoAttackConfig defaults() {
        return new Builder()
                .enabled(false)
                .limitRange(false)
                .maxRange(2.5)
                .extraTicksMin(0)
                .extraTicksMax(0)
                .build();
    }

    // <generated-code>

    public AutoAttackConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoAttackConfig withLimitRange(boolean limitRange) {
        return Builder.from(this).limitRange(limitRange).build();
    }

    public AutoAttackConfig withMaxRange(double maxRange) {
        return Builder.from(this).maxRange(maxRange).build();
    }

    public AutoAttackConfig withExtraTicksMin(int extraTicksMin) {
        return Builder.from(this).extraTicksMin(extraTicksMin).build();
    }

    public AutoAttackConfig withExtraTicksMax(int extraTicksMax) {
        return Builder.from(this).extraTicksMax(extraTicksMax).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean limitRange;
        private double maxRange;
        private int extraTicksMin;
        private int extraTicksMax;

        private Builder() {}

        public static Builder from(AutoAttackConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .limitRange(config.limitRange)
                    .maxRange(config.maxRange)
                    .extraTicksMin(config.extraTicksMin)
                    .extraTicksMax(config.extraTicksMax);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder limitRange(boolean limitRange) {
            this.limitRange = limitRange;
            return this;
        }

        public Builder maxRange(double maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public Builder extraTicksMin(int extraTicksMin) {
            this.extraTicksMin = extraTicksMin;
            return this;
        }

        public Builder extraTicksMax(int extraTicksMax) {
            this.extraTicksMax = extraTicksMax;
            return this;
        }

        public AutoAttackConfig build() {
            return new AutoAttackConfig(enabled, limitRange, maxRange, extraTicksMin, extraTicksMax);
        }
    }

    // </generated-code>
}