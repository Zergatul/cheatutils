package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AutoFishConfig(
        boolean enabled,
        boolean autoRestartOnIdle,
        int idleTimeout
) implements ImmutableConfig, ToggleableConfig<AutoFishConfig> {

    public AutoFishConfig {
        idleTimeout = MathUtils.clamp(idleTimeout, 5, 3600);
    }

    public static AutoFishConfig defaults() {
        return new Builder()
                .enabled(false)
                .autoRestartOnIdle(false)
                .idleTimeout(60)
                .build();
    }

    // <generated-code>

    public AutoFishConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoFishConfig withAutoRestartOnIdle(boolean autoRestartOnIdle) {
        return Builder.from(this).autoRestartOnIdle(autoRestartOnIdle).build();
    }

    public AutoFishConfig withIdleTimeout(int idleTimeout) {
        return Builder.from(this).idleTimeout(idleTimeout).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean autoRestartOnIdle;
        private int idleTimeout;

        private Builder() {}

        public static Builder from(AutoFishConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .autoRestartOnIdle(config.autoRestartOnIdle)
                    .idleTimeout(config.idleTimeout);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder autoRestartOnIdle(boolean autoRestartOnIdle) {
            this.autoRestartOnIdle = autoRestartOnIdle;
            return this;
        }

        public Builder idleTimeout(int idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        public AutoFishConfig build() {
            return new AutoFishConfig(enabled, autoRestartOnIdle, idleTimeout);
        }
    }

    // </generated-code>
}
