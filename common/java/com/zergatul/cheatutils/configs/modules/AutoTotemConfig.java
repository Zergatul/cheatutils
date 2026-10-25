package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record AutoTotemConfig(
        boolean enabled,
        boolean skipIfUsingItem
) implements ImmutableConfig, ToggleableConfig<AutoTotemConfig> {

    public static AutoTotemConfig defaults() {
        return new Builder()
                .enabled(false)
                .skipIfUsingItem(false)
                .build();
    }

    // <generated-code>

    public AutoTotemConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoTotemConfig withSkipIfUsingItem(boolean skipIfUsingItem) {
        return Builder.from(this).skipIfUsingItem(skipIfUsingItem).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean skipIfUsingItem;

        private Builder() {}

        public static Builder from(AutoTotemConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .skipIfUsingItem(config.skipIfUsingItem);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder skipIfUsingItem(boolean skipIfUsingItem) {
            this.skipIfUsingItem = skipIfUsingItem;
            return this;
        }

        public AutoTotemConfig build() {
            return new AutoTotemConfig(enabled, skipIfUsingItem);
        }
    }

    // </generated-code>
}