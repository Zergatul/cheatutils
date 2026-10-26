package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record UserNameConfig(
        boolean enabled,
        String name,
        boolean showNameInTitle
) implements ImmutableConfig, ToggleableConfig<UserNameConfig> {

    public static UserNameConfig defaults() {
        return new Builder()
                .enabled(false)
                .name(null)
                .showNameInTitle(false)
                .build();
    }

    // <generated-code>

    public UserNameConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public UserNameConfig withName(String name) {
        return Builder.from(this).name(name).build();
    }

    public UserNameConfig withShowNameInTitle(boolean showNameInTitle) {
        return Builder.from(this).showNameInTitle(showNameInTitle).build();
    }

    public static class Builder {

        private boolean enabled;
        private String name;
        private boolean showNameInTitle;

        private Builder() {}

        public static Builder from(UserNameConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .name(config.name)
                    .showNameInTitle(config.showNameInTitle);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder showNameInTitle(boolean showNameInTitle) {
            this.showNameInTitle = showNameInTitle;
            return this;
        }

        public UserNameConfig build() {
            return new UserNameConfig(enabled, name, showNameInTitle);
        }
    }

    // </generated-code>
}