package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record BreachSwapConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<BreachSwapConfig> {

    public static BreachSwapConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public BreachSwapConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(BreachSwapConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public BreachSwapConfig build() {
            return new BreachSwapConfig(enabled);
        }
    }

    // </generated-code>
}