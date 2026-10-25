package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record EndCityChunksConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<EndCityChunksConfig> {

    public static EndCityChunksConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public EndCityChunksConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(EndCityChunksConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public EndCityChunksConfig build() {
            return new EndCityChunksConfig(enabled);
        }
    }

    // </generated-code>
}