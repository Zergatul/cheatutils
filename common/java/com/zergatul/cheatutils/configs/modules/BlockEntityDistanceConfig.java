package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record BlockEntityDistanceConfig(
        boolean enabled,
        int viewDistance
) implements ImmutableConfig, ToggleableConfig<BlockEntityDistanceConfig> {

    public BlockEntityDistanceConfig {
        viewDistance = MathUtils.clamp(viewDistance, 0, 65536);
    }

    public static BlockEntityDistanceConfig defaults() {
        return new Builder()
                .enabled(false)
                .viewDistance(128)
                .build();
    }

    // <generated-code>

    public BlockEntityDistanceConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public BlockEntityDistanceConfig withViewDistance(int viewDistance) {
        return Builder.from(this).viewDistance(viewDistance).build();
    }

    public static class Builder {

        private boolean enabled;
        private int viewDistance;

        private Builder() {}

        public static Builder from(BlockEntityDistanceConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .viewDistance(config.viewDistance);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder viewDistance(int viewDistance) {
            this.viewDistance = viewDistance;
            return this;
        }

        public BlockEntityDistanceConfig build() {
            return new BlockEntityDistanceConfig(enabled, viewDistance);
        }
    }

    // </generated-code>
}