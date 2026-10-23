package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record AutoCriticalsConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<AutoCriticalsConfig> {

    public static AutoCriticalsConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public AutoCriticalsConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(AutoCriticalsConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AutoCriticalsConfig build() {
            return new AutoCriticalsConfig(enabled);
        }
    }

    // </generated-code>
}