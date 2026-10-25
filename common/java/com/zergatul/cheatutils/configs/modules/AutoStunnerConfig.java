package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record AutoStunnerConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<AutoStunnerConfig> {

    public static AutoStunnerConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public AutoStunnerConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(AutoStunnerConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AutoStunnerConfig build() {
            return new AutoStunnerConfig(enabled);
        }
    }

    // </generated-code>
}