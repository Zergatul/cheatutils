package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record StepUpConfig(
        boolean enabled,
        double height
) implements ImmutableConfig, ToggleableConfig<StepUpConfig> {

    public StepUpConfig {
        height = MathUtils.clamp(height, 0, 100);
    }

    public static StepUpConfig defaults() {
        return new Builder()
                .enabled(false)
                .height(1)
                .build();
    }

    // <generated-code>

    public StepUpConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public StepUpConfig withHeight(double height) {
        return Builder.from(this).height(height).build();
    }

    public static class Builder {

        private boolean enabled;
        private double height;

        private Builder() {}

        public static Builder from(StepUpConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .height(config.height);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder height(double height) {
            this.height = height;
            return this;
        }

        public StepUpConfig build() {
            return new StepUpConfig(enabled, height);
        }
    }

    // </generated-code>
}