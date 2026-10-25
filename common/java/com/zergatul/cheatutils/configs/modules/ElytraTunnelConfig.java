package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ElytraTunnelConfig(
        boolean enabled,
        double limit
) implements ImmutableConfig, ToggleableConfig<ElytraTunnelConfig> {

    public ElytraTunnelConfig {
        limit = MathUtils.clamp(limit, -1000, 1000);
    }

    public static ElytraTunnelConfig defaults() {
        return new Builder()
                .enabled(false)
                .limit(122.8)
                .build();
    }

    // <generated-code>

    public ElytraTunnelConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ElytraTunnelConfig withLimit(double limit) {
        return Builder.from(this).limit(limit).build();
    }

    public static class Builder {

        private boolean enabled;
        private double limit;

        private Builder() {}

        public static Builder from(ElytraTunnelConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .limit(config.limit);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder limit(double limit) {
            this.limit = limit;
            return this;
        }

        public ElytraTunnelConfig build() {
            return new ElytraTunnelConfig(enabled, limit);
        }
    }

    // </generated-code>
}