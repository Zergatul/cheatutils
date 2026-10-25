package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record DeathCoordinatesConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<DeathCoordinatesConfig> {

    public static DeathCoordinatesConfig defaults() {
        return new Builder()
                .enabled(true)
                .build();
    }

    // <generated-code>

    public DeathCoordinatesConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(DeathCoordinatesConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public DeathCoordinatesConfig build() {
            return new DeathCoordinatesConfig(enabled);
        }
    }

    // </generated-code>
}