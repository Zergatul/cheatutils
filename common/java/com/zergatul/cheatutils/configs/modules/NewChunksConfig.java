package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record NewChunksConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<NewChunksConfig> {

    public static NewChunksConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public NewChunksConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(NewChunksConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public NewChunksConfig build() {
            return new NewChunksConfig(enabled);
        }
    }

    // </generated-code>
}