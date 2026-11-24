package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record FullBrightConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<FullBrightConfig> {

    public static FullBrightConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public FullBrightConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(FullBrightConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public FullBrightConfig build() {
            return new FullBrightConfig(enabled);
        }
    }

    // </generated-code>
}