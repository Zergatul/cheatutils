package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AutoEatConfig(
        boolean enabled,
        boolean isHungerLimitEnabled,
        int hungerLimit
) implements ImmutableConfig, ToggleableConfig<AutoEatConfig> {

    public AutoEatConfig {
        hungerLimit = MathUtils.clamp(hungerLimit, 0, 100);
    }

    public static AutoEatConfig defaults() {
        return new Builder()
                .enabled(false)
                .isHungerLimitEnabled(false)
                .hungerLimit(10)
                .build();
    }

    // <generated-code>

    public AutoEatConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoEatConfig withIsHungerLimitEnabled(boolean isHungerLimitEnabled) {
        return Builder.from(this).isHungerLimitEnabled(isHungerLimitEnabled).build();
    }

    public AutoEatConfig withHungerLimit(int hungerLimit) {
        return Builder.from(this).hungerLimit(hungerLimit).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean isHungerLimitEnabled;
        private int hungerLimit;

        private Builder() {}

        public static Builder from(AutoEatConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .isHungerLimitEnabled(config.isHungerLimitEnabled)
                    .hungerLimit(config.hungerLimit);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder isHungerLimitEnabled(boolean isHungerLimitEnabled) {
            this.isHungerLimitEnabled = isHungerLimitEnabled;
            return this;
        }

        public Builder hungerLimit(int hungerLimit) {
            this.hungerLimit = hungerLimit;
            return this;
        }

        public AutoEatConfig build() {
            return new AutoEatConfig(enabled, isHungerLimitEnabled, hungerLimit);
        }
    }

    // </generated-code>
}