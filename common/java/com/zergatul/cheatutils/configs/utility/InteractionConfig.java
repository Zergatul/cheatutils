package com.zergatul.cheatutils.configs.utility;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

// TODO: for future use
public record InteractionConfig(
        double maxRange,
        boolean autoRotate
) implements ImmutableConfig {

    public InteractionConfig {
        maxRange = MathUtils.clamp(maxRange, 1, 10);
    }

    public static InteractionConfig defaults() {
        return new Builder()
                .maxRange(4)
                .autoRotate(false)
                .build();
    }

    // <generated-code>

    public InteractionConfig withMaxRange(double maxRange) {
        return Builder.from(this).maxRange(maxRange).build();
    }

    public InteractionConfig withAutoRotate(boolean autoRotate) {
        return Builder.from(this).autoRotate(autoRotate).build();
    }

    public static class Builder {

        private double maxRange;
        private boolean autoRotate;

        private Builder() {}

        public static Builder from(InteractionConfig config) {
            return new Builder()
                    .maxRange(config.maxRange)
                    .autoRotate(config.autoRotate);
        }

        public Builder maxRange(double maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public Builder autoRotate(boolean autoRotate) {
            this.autoRotate = autoRotate;
            return this;
        }

        public InteractionConfig build() {
            return new InteractionConfig(maxRange, autoRotate);
        }
    }

    // </generated-code>
}