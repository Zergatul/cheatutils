package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record ExplorationMiniMapConfig(
        boolean enabled,
        Integer scanFromY
) implements ImmutableConfig, ToggleableConfig<ExplorationMiniMapConfig> {

    public ExplorationMiniMapConfig {
        if (scanFromY != null) {
            scanFromY = MathUtils.clamp(scanFromY, -1000, 1000);
        }
    }

    public static ExplorationMiniMapConfig defaults() {
        return new Builder()
                .enabled(false)
                .scanFromY(null)
                .build();
    }

    // <generated-code>

    public ExplorationMiniMapConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public ExplorationMiniMapConfig withScanFromY(Integer scanFromY) {
        return Builder.from(this).scanFromY(scanFromY).build();
    }

    public static class Builder {

        private boolean enabled;
        private Integer scanFromY;

        private Builder() {}

        public static Builder from(ExplorationMiniMapConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .scanFromY(config.scanFromY);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder scanFromY(Integer scanFromY) {
            this.scanFromY = scanFromY;
            return this;
        }

        public ExplorationMiniMapConfig build() {
            return new ExplorationMiniMapConfig(enabled, scanFromY);
        }
    }

    // </generated-code>
}