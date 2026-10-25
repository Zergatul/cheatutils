package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.configs.utility.FontConfig;

public record StatusOverlayConfig(
        boolean enabled,
        FontConfig font,
        String code
) implements ImmutableConfigWithUpdateOptions<StatusOverlayConfig.UpdateOptions>, ToggleableConfig<StatusOverlayConfig> {

    public StatusOverlayConfig {
        if (font == null) {
            font = FontConfig.defaults();
        }
    }

    public static StatusOverlayConfig defaults() {
        return new Builder()
                .enabled(false)
                .font(FontConfig.defaults())
                .code(null)
                .build();
    }

    // <generated-code>

    public StatusOverlayConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public StatusOverlayConfig withFont(FontConfig font) {
        return Builder.from(this).font(font).build();
    }

    public StatusOverlayConfig withCode(String code) {
        return Builder.from(this).code(code).build();
    }

    @Override
    public StatusOverlayConfig withOptions(UpdateOptions options) {
        return Builder.from(this)
                .enabled(options.enabled())
                .font(options.font())
                .build();
    }

    public record UpdateOptions(
            boolean enabled,
            FontConfig font
    ) {}

    public static class Builder {

        private boolean enabled;
        private FontConfig font;
        private String code;

        private Builder() {}

        public static Builder from(StatusOverlayConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .font(config.font)
                    .code(config.code);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder font(FontConfig font) {
            this.font = font;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public StatusOverlayConfig build() {
            return new StatusOverlayConfig(enabled, font, code);
        }
    }

    // </generated-code>
}