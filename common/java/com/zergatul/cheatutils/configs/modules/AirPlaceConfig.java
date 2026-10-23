package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

@SuppressWarnings("unused")
public record AirPlaceConfig(
        double minRange,
        double maxRange
) implements ImmutableConfig {

    public AirPlaceConfig {
        minRange = MathUtils.clamp(minRange, 0.5, 5);
        maxRange = MathUtils.clamp(maxRange, minRange, 10);
    }

    public static AirPlaceConfig defaults() {
        return new Builder()
                .minRange(1)
                .maxRange(5)
                .build();
    }

    // <generated-code>

    public AirPlaceConfig withMinRange(double minRange) {
        return Builder.from(this).minRange(minRange).build();
    }

    public AirPlaceConfig withMaxRange(double maxRange) {
        return Builder.from(this).maxRange(maxRange).build();
    }

    public static class Builder {

        private double minRange;
        private double maxRange;

        private Builder() {}

        public static Builder from(AirPlaceConfig config) {
            return new Builder()
                    .minRange(config.minRange)
                    .maxRange(config.maxRange);
        }

        public AirPlaceConfig.Builder minRange(double minRange) {
            this.minRange = minRange;
            return this;
        }

        public AirPlaceConfig.Builder maxRange(double maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public AirPlaceConfig build() {
            return new AirPlaceConfig(minRange, maxRange);
        }
    }

    // </generated-code>
}