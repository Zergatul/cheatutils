package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record EventsScriptingConfig(
        boolean enabled,
        String code
) implements ImmutableConfigWithUpdateOptions<EventsScriptingConfig.UpdateOptions>, ToggleableConfig<EventsScriptingConfig> {

    public static EventsScriptingConfig defaults() {
        return new Builder()
                .enabled(false)
                .code(null)
                .build();
    }

    // <generated-code>

    public EventsScriptingConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public EventsScriptingConfig withCode(String code) {
        return Builder.from(this).code(code).build();
    }

    @Override
    public EventsScriptingConfig withOptions(UpdateOptions options) {
        return Builder.from(this)
                .enabled(options.enabled())
                .build();
    }

    public record UpdateOptions(
            boolean enabled
    ) {}

    public static class Builder {

        private boolean enabled;
        private String code;

        private Builder() {}

        public static Builder from(EventsScriptingConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .code(config.code);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public EventsScriptingConfig build() {
            return new EventsScriptingConfig(enabled, code);
        }
    }

    // </generated-code>
}