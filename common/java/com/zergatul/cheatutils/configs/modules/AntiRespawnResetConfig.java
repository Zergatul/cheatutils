package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record AntiRespawnResetConfig(
        boolean enabled
) implements ImmutableConfig, ToggleableConfig<AntiRespawnResetConfig> {

    public static AntiRespawnResetConfig defaults() {
        return new Builder()
                .enabled(false)
                .build();
    }

    // <generated-code>

    public AntiRespawnResetConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private boolean enabled;

        private Builder() {}

        public static Builder from(AntiRespawnResetConfig config) {
            return new Builder()
                    .enabled(config.enabled);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AntiRespawnResetConfig build() {
            return new AntiRespawnResetConfig(enabled);
        }
    }

    // </generated-code>
}