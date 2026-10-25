package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record LogoutSpotsConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<LogoutSpotsConfig> {

    public static LogoutSpotsConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public LogoutSpotsConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(LogoutSpotsConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public LogoutSpotsConfig build() {
            return new LogoutSpotsConfig(enabled);
        }
    }

    // </generated-code>
}