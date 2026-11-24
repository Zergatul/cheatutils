package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record ShulkerTooltipConfig(
        boolean enabled,
        boolean showOriginal
) implements ImmutableConfig, ToggleableConfig<ShulkerTooltipConfig> {

    public static ShulkerTooltipConfig defaults() {
        return new Builder()
                .enabled(true)
                .showOriginal(false)
                .build();
    }

    // <generated-code>

    public ShulkerTooltipConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ShulkerTooltipConfig withShowOriginal(boolean showOriginal) {
        return Builder.from(this).showOriginal(showOriginal).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean showOriginal;

        private Builder() {}

        public static Builder from(ShulkerTooltipConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .showOriginal(config.showOriginal);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder showOriginal(boolean showOriginal) {
            this.showOriginal = showOriginal;
            return this;
        }

        public ShulkerTooltipConfig build() {
            return new ShulkerTooltipConfig(enabled, showOriginal);
        }
    }

    // </generated-code>
}