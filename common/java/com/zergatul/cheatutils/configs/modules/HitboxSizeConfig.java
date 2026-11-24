package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

import java.util.Objects;

public record HitboxSizeConfig(
        boolean enabled,
        String widthMode,
        double widthPercent,
        double widthAbsolute,
        String heightMode,
        double heightPercent,
        double heightAbsolute
) implements ImmutableConfig, ToggleableConfig<HitboxSizeConfig> {

    public static final String MODE_PERCENT = "PERCENT";
    public static final String MODE_ABSOLUTE = "ABSOLUTE";

    public HitboxSizeConfig {
        if (!Objects.equals(widthMode, MODE_PERCENT) && !Objects.equals(widthMode, MODE_ABSOLUTE)) {
            widthMode = MODE_PERCENT;
        }

        if (!Objects.equals(heightMode, MODE_PERCENT) && !Objects.equals(heightMode, MODE_ABSOLUTE)) {
            heightMode = MODE_PERCENT;
        }

        widthPercent = MathUtils.clamp(widthPercent, -99, 1000);
        heightPercent = MathUtils.clamp(heightPercent, -99, 1000);
        widthAbsolute = MathUtils.clamp(widthAbsolute, 0, 100);
        heightAbsolute = MathUtils.clamp(heightAbsolute, 0, 100);
    }

    public static HitboxSizeConfig defaults() {
        return new Builder()
                .enabled(false)
                .widthMode(MODE_PERCENT)
                .widthPercent(25)
                .widthAbsolute(0.5)
                .heightMode(MODE_PERCENT)
                .heightPercent(0)
                .heightAbsolute(0)
                .build();
    }

    // <generated-code>

    public HitboxSizeConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public HitboxSizeConfig withWidthMode(String widthMode) {
        return Builder.from(this).widthMode(widthMode).build();
    }

    public HitboxSizeConfig withWidthPercent(double widthPercent) {
        return Builder.from(this).widthPercent(widthPercent).build();
    }

    public HitboxSizeConfig withWidthAbsolute(double widthAbsolute) {
        return Builder.from(this).widthAbsolute(widthAbsolute).build();
    }

    public HitboxSizeConfig withHeightMode(String heightMode) {
        return Builder.from(this).heightMode(heightMode).build();
    }

    public HitboxSizeConfig withHeightPercent(double heightPercent) {
        return Builder.from(this).heightPercent(heightPercent).build();
    }

    public HitboxSizeConfig withHeightAbsolute(double heightAbsolute) {
        return Builder.from(this).heightAbsolute(heightAbsolute).build();
    }

    public static class Builder {

        private boolean enabled;
        private String widthMode;
        private double widthPercent;
        private double widthAbsolute;
        private String heightMode;
        private double heightPercent;
        private double heightAbsolute;

        private Builder() {}

        public static Builder from(HitboxSizeConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .widthMode(config.widthMode)
                    .widthPercent(config.widthPercent)
                    .widthAbsolute(config.widthAbsolute)
                    .heightMode(config.heightMode)
                    .heightPercent(config.heightPercent)
                    .heightAbsolute(config.heightAbsolute);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder widthMode(String widthMode) {
            this.widthMode = widthMode;
            return this;
        }

        public Builder widthPercent(double widthPercent) {
            this.widthPercent = widthPercent;
            return this;
        }

        public Builder widthAbsolute(double widthAbsolute) {
            this.widthAbsolute = widthAbsolute;
            return this;
        }

        public Builder heightMode(String heightMode) {
            this.heightMode = heightMode;
            return this;
        }

        public Builder heightPercent(double heightPercent) {
            this.heightPercent = heightPercent;
            return this;
        }

        public Builder heightAbsolute(double heightAbsolute) {
            this.heightAbsolute = heightAbsolute;
            return this;
        }

        public HitboxSizeConfig build() {
            return new HitboxSizeConfig(enabled, widthMode, widthPercent, widthAbsolute, heightMode, heightPercent, heightAbsolute);
        }
    }

    // </generated-code>
}