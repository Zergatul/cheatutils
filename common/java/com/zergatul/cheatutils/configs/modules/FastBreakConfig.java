package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record FastBreakConfig(
        boolean enabled,
        double factor,
        boolean disableDestroyDelay
) implements ImmutableConfig, ToggleableConfig<FastBreakConfig> {

    public FastBreakConfig {
        factor = MathUtils.clamp(factor, 0.5, 10);
    }

    public static FastBreakConfig defaults() {
        return new Builder()
                .enabled(false)
                .factor(1.2)
                .disableDestroyDelay(false)
                .build();
    }

    // <generated-code>

    public FastBreakConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public FastBreakConfig withFactor(double factor) {
        return Builder.from(this).factor(factor).build();
    }

    public FastBreakConfig withDisableDestroyDelay(boolean disableDestroyDelay) {
        return Builder.from(this).disableDestroyDelay(disableDestroyDelay).build();
    }

    public static class Builder {

        private boolean enabled;
        private double factor;
        private boolean disableDestroyDelay;

        private Builder() {}

        public static Builder from(FastBreakConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .factor(config.factor)
                    .disableDestroyDelay(config.disableDestroyDelay);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder factor(double factor) {
            this.factor = factor;
            return this;
        }

        public Builder disableDestroyDelay(boolean disableDestroyDelay) {
            this.disableDestroyDelay = disableDestroyDelay;
            return this;
        }

        public FastBreakConfig build() {
            return new FastBreakConfig(enabled, factor, disableDestroyDelay);
        }
    }

    // </generated-code>
}