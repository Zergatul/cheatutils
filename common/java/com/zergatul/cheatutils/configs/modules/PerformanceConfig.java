package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import com.zergatul.cheatutils.utils.MathUtils;

public record PerformanceConfig(
        boolean limitBackgroundWindowFps,
        int backgroundWindowFps
) implements ImmutableConfig, ModuleStateProvider {

    public PerformanceConfig {
        backgroundWindowFps = MathUtils.clamp(backgroundWindowFps, 1, 120);
    }

    public static PerformanceConfig defaults() {
        return new Builder()
                .limitBackgroundWindowFps(false)
                .backgroundWindowFps(20)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return limitBackgroundWindowFps;
    }

    // <generated-code>

    public PerformanceConfig withLimitBackgroundWindowFps(boolean limitBackgroundWindowFps) {
        return Builder.from(this).limitBackgroundWindowFps(limitBackgroundWindowFps).build();
    }

    public PerformanceConfig withBackgroundWindowFps(int backgroundWindowFps) {
        return Builder.from(this).backgroundWindowFps(backgroundWindowFps).build();
    }

    public static class Builder {

        private boolean limitBackgroundWindowFps;
        private int backgroundWindowFps;

        private Builder() {}

        public static Builder from(PerformanceConfig config) {
            return new Builder()
                    .limitBackgroundWindowFps(config.limitBackgroundWindowFps)
                    .backgroundWindowFps(config.backgroundWindowFps);
        }

        public Builder limitBackgroundWindowFps(boolean limitBackgroundWindowFps) {
            this.limitBackgroundWindowFps = limitBackgroundWindowFps;
            return this;
        }

        public Builder backgroundWindowFps(int backgroundWindowFps) {
            this.backgroundWindowFps = backgroundWindowFps;
            return this;
        }

        public PerformanceConfig build() {
            return new PerformanceConfig(limitBackgroundWindowFps, backgroundWindowFps);
        }
    }

    // </generated-code>
}