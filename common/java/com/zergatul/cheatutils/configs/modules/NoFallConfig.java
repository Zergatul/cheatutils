package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record NoFallConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<NoFallConfig> {

    public static NoFallConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public NoFallConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(NoFallConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public NoFallConfig build() {
            return new NoFallConfig(enabled);
        }
    }

    // </generated-code>
}
