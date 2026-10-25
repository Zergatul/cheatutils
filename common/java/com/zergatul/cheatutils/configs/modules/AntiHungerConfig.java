package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record AntiHungerConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<AntiHungerConfig> {

    public static AntiHungerConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public AntiHungerConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(AntiHungerConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AntiHungerConfig build() {
            return new AntiHungerConfig(enabled);
        }
    }

    // </generated-code>
}