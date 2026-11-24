package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record TridentHackConfig(
        boolean enabled,
        boolean allowOutOfWater,
        double boost
) implements ImmutableConfig, ToggleableConfig<TridentHackConfig> {

    public TridentHackConfig {
        boost = MathUtils.clamp(boost, 0.1, 1000.0);
    }

    public static TridentHackConfig defaults() {
        return new Builder()
                .enabled(false)
                .allowOutOfWater(false)
                .boost(1.0)
                .build();
    }

    // <generated-code>

    public TridentHackConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public TridentHackConfig withAllowOutOfWater(boolean allowOutOfWater) {
        return Builder.from(this).allowOutOfWater(allowOutOfWater).build();
    }

    public TridentHackConfig withBoost(double boost) {
        return Builder.from(this).boost(boost).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean allowOutOfWater;
        private double boost;

        private Builder() {}

        public static Builder from(TridentHackConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .allowOutOfWater(config.allowOutOfWater)
                    .boost(config.boost);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder allowOutOfWater(boolean allowOutOfWater) {
            this.allowOutOfWater = allowOutOfWater;
            return this;
        }

        public Builder boost(double boost) {
            this.boost = boost;
            return this;
        }

        public TridentHackConfig build() {
            return new TridentHackConfig(enabled, allowOutOfWater, boost);
        }
    }

    // </generated-code>
}