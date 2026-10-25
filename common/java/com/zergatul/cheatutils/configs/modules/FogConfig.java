package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record FogConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<FogConfig> {

    public static FogConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public FogConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(FogConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public FogConfig build() {
            return new FogConfig(enabled);
        }
    }

    // </generated-code>
}