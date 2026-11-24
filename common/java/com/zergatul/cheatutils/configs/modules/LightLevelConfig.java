package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record LightLevelConfig(
        boolean enabled,
        boolean showLocations,
        boolean showTracers,
        boolean showLightLevelValue,
        boolean useFreeCamPosition,
        float maxDistance
) implements ImmutableConfig, ToggleableConfig<LightLevelConfig> {

    public LightLevelConfig {
        maxDistance = MathUtils.clamp(maxDistance, 1, 1000);
    }

    public static LightLevelConfig defaults() {
        return new Builder()
                .enabled(false)
                .showLocations(false)
                .showTracers(true)
                .showLightLevelValue(false)
                .useFreeCamPosition(false)
                .maxDistance(20)
                .build();
    }

    // <generated-code>

    public LightLevelConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public LightLevelConfig withShowLocations(boolean showLocations) {
        return Builder.from(this).showLocations(showLocations).build();
    }

    public LightLevelConfig withShowTracers(boolean showTracers) {
        return Builder.from(this).showTracers(showTracers).build();
    }

    public LightLevelConfig withShowLightLevelValue(boolean showLightLevelValue) {
        return Builder.from(this).showLightLevelValue(showLightLevelValue).build();
    }

    public LightLevelConfig withUseFreeCamPosition(boolean useFreeCamPosition) {
        return Builder.from(this).useFreeCamPosition(useFreeCamPosition).build();
    }

    public LightLevelConfig withMaxDistance(float maxDistance) {
        return Builder.from(this).maxDistance(maxDistance).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean showLocations;
        private boolean showTracers;
        private boolean showLightLevelValue;
        private boolean useFreeCamPosition;
        private float maxDistance;

        private Builder() {}

        public static Builder from(LightLevelConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .showLocations(config.showLocations)
                    .showTracers(config.showTracers)
                    .showLightLevelValue(config.showLightLevelValue)
                    .useFreeCamPosition(config.useFreeCamPosition)
                    .maxDistance(config.maxDistance);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder showLocations(boolean showLocations) {
            this.showLocations = showLocations;
            return this;
        }

        public Builder showTracers(boolean showTracers) {
            this.showTracers = showTracers;
            return this;
        }

        public Builder showLightLevelValue(boolean showLightLevelValue) {
            this.showLightLevelValue = showLightLevelValue;
            return this;
        }

        public Builder useFreeCamPosition(boolean useFreeCamPosition) {
            this.useFreeCamPosition = useFreeCamPosition;
            return this;
        }

        public Builder maxDistance(float maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        public LightLevelConfig build() {
            return new LightLevelConfig(enabled, showLocations, showTracers, showLightLevelValue, useFreeCamPosition, maxDistance);
        }
    }

    // </generated-code>
}