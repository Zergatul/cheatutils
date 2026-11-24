package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AreaMineConfig(
        boolean enabled,
        double radius,
        boolean preview
) implements ImmutableConfig, ToggleableConfig<AreaMineConfig> {

    public AreaMineConfig {
        radius = MathUtils.clamp(radius, 1, 20);
    }

    public static AreaMineConfig defaults() {
        return new Builder()
                .enabled(false)
                .radius(3)
                .preview(true)
                .build();
    }

    // <generated-code>

    public AreaMineConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AreaMineConfig withRadius(double radius) {
        return Builder.from(this).radius(radius).build();
    }

    public AreaMineConfig withPreview(boolean preview) {
        return Builder.from(this).preview(preview).build();
    }

    public static class Builder {

        private boolean enabled;
        private double radius;
        private boolean preview;

        private Builder() {}

        public static Builder from(AreaMineConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .radius(config.radius)
                    .preview(config.preview);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder preview(boolean preview) {
            this.preview = preview;
            return this;
        }

        public AreaMineConfig build() {
            return new AreaMineConfig(enabled, radius, preview);
        }
    }

    // </generated-code>
}