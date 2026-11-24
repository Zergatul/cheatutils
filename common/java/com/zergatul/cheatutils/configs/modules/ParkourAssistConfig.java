package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ParkourAssistConfig(
        double threshold
) implements ImmutableConfig {

    public ParkourAssistConfig {
        threshold = MathUtils.clamp(threshold, 0, 100);
    }

    public static ParkourAssistConfig defaults() {
        return new Builder()
                .threshold(0.25)
                .build();
    }

    // <generated-code>

    public ParkourAssistConfig withThreshold(double threshold) {
        return Builder.from(this).threshold(threshold).build();
    }

    public static class Builder {

        private double threshold;

        private Builder() {}

        public static Builder from(ParkourAssistConfig config) {
            return new Builder()
                    .threshold(config.threshold);
        }

        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public ParkourAssistConfig build() {
            return new ParkourAssistConfig(threshold);
        }
    }

    // </generated-code>
}