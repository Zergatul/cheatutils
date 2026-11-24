package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record SpearRangeConfig(
        boolean enabled,
        boolean lungeWithoutTarget
) implements ImmutableConfig, ToggleableConfig<SpearRangeConfig> {

    public static SpearRangeConfig defaults() {
        return new Builder()
                .enabled(false)
                .lungeWithoutTarget(false)
                .build();
    }

    // <generated-code>

    public SpearRangeConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public SpearRangeConfig withLungeWithoutTarget(boolean lungeWithoutTarget) {
        return Builder.from(this).lungeWithoutTarget(lungeWithoutTarget).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean lungeWithoutTarget;

        private Builder() {}

        public static Builder from(SpearRangeConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .lungeWithoutTarget(config.lungeWithoutTarget);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder lungeWithoutTarget(boolean lungeWithoutTarget) {
            this.lungeWithoutTarget = lungeWithoutTarget;
            return this;
        }

        public SpearRangeConfig build() {
            return new SpearRangeConfig(enabled, lungeWithoutTarget);
        }
    }

    // </generated-code>
}