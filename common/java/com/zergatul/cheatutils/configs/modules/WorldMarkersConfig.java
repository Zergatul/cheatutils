package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.configs.utility.FontConfig;
import com.zergatul.cheatutils.utils.MathUtils;

import java.util.Objects;

public record WorldMarkersConfig(
        boolean enabled,
        ImmutableList<WorldMarkerEntry> entries,
        FontConfig font,
        int borderWidth
) implements ImmutableConfig, ToggleableConfig<WorldMarkersConfig> {

    public WorldMarkersConfig {
        if (entries == null) {
            entries = ImmutableList.empty();
        }
        entries = entries.removeAll(Objects::isNull);

        if (font == null) {
            font = FontConfig.defaults();
        }

        borderWidth = MathUtils.clamp(borderWidth, 1, 10);
    }

    public static WorldMarkersConfig defaults() {
        return new Builder()
                .enabled(false)
                .entries(ImmutableList.empty())
                .font(FontConfig.defaults())
                .borderWidth(1)
                .build();
    }

    // <generated-code>

    public WorldMarkersConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public WorldMarkersConfig withEntries(ImmutableList<WorldMarkerEntry> entries) {
        return Builder.from(this).entries(entries).build();
    }

    public WorldMarkersConfig withFont(FontConfig font) {
        return Builder.from(this).font(font).build();
    }

    public WorldMarkersConfig withBorderWidth(int borderWidth) {
        return Builder.from(this).borderWidth(borderWidth).build();
    }

    public static class Builder {

        private boolean enabled;
        private ImmutableList<WorldMarkerEntry> entries;
        private FontConfig font;
        private int borderWidth;

        private Builder() {}

        public static Builder from(WorldMarkersConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .entries(config.entries)
                    .font(config.font)
                    .borderWidth(config.borderWidth);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder entries(ImmutableList<WorldMarkerEntry> entries) {
            this.entries = entries;
            return this;
        }

        public Builder font(FontConfig font) {
            this.font = font;
            return this;
        }

        public Builder borderWidth(int borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public WorldMarkersConfig build() {
            return new WorldMarkersConfig(enabled, entries, font, borderWidth);
        }
    }

    // </generated-code>
}