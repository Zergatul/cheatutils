package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record ArmorOverlayConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<ArmorOverlayConfig> {

    public static ArmorOverlayConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public ArmorOverlayConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(ArmorOverlayConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ArmorOverlayConfig build() {
            return new ArmorOverlayConfig(enabled);
        }
    }

    // </generated-code>
}