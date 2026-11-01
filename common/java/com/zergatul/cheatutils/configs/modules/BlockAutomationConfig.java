package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.configs.utility.BlockPlacerConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record BlockAutomationConfig(
        boolean enabled,
        double maxRange,
        ImmutableList<Integer> autoSelectSlots,
        boolean attachToAir,
        boolean useShift,
        double placementRate,
        boolean autoRotate,
        boolean debugMode,
        String code
) implements ImmutableConfigWithUpdateOptions<BlockAutomationConfig.UpdateOptions>, ToggleableConfig<BlockAutomationConfig>, BlockPlacerConfig {

    public BlockAutomationConfig {
        maxRange = MathUtils.clamp(maxRange, 1, 10);
        autoSelectSlots = BlockPlacerConfig.sanitizeAutoSelectSlots(autoSelectSlots);
        placementRate = MathUtils.clamp(placementRate, 1, 100);
    }

    public static BlockAutomationConfig defaults() {
        return new Builder()
                .enabled(false)
                .maxRange(5)
                .autoSelectSlots(ImmutableList.<Integer>empty().add(9))
                .attachToAir(false)
                .useShift(false)
                .placementRate(1)
                .autoRotate(false)
                .debugMode(false)
                .code(null)
                .build();
    }

    @Override
    public ImmutableList<Integer> getAutoSelectSlots() {
        return autoSelectSlots;
    }

    @Override
    public boolean shouldAttachToAir() {
        return attachToAir;
    }

    @Override
    public boolean shouldUseShift() {
        return useShift;
    }

    @Override
    public double getPlacementRate() {
        return placementRate;
    }

    @Override
    public double getMaxRange() {
        return maxRange;
    }

    @Override
    public boolean shouldAutoRotate() {
        return autoRotate;
    }

    // <generated-code>

    public BlockAutomationConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public BlockAutomationConfig withMaxRange(double maxRange) {
        return Builder.from(this).maxRange(maxRange).build();
    }

    public BlockAutomationConfig withAutoSelectSlots(ImmutableList<Integer> autoSelectSlots) {
        return Builder.from(this).autoSelectSlots(autoSelectSlots).build();
    }

    public BlockAutomationConfig withAttachToAir(boolean attachToAir) {
        return Builder.from(this).attachToAir(attachToAir).build();
    }

    public BlockAutomationConfig withUseShift(boolean useShift) {
        return Builder.from(this).useShift(useShift).build();
    }

    public BlockAutomationConfig withPlacementRate(double placementRate) {
        return Builder.from(this).placementRate(placementRate).build();
    }

    public BlockAutomationConfig withAutoRotate(boolean autoRotate) {
        return Builder.from(this).autoRotate(autoRotate).build();
    }

    public BlockAutomationConfig withDebugMode(boolean debugMode) {
        return Builder.from(this).debugMode(debugMode).build();
    }

    public BlockAutomationConfig withCode(String code) {
        return Builder.from(this).code(code).build();
    }

    @Override
    public BlockAutomationConfig withOptions(UpdateOptions options) {
        return Builder.from(this)
                .enabled(options.enabled())
                .maxRange(options.maxRange())
                .autoSelectSlots(options.autoSelectSlots())
                .attachToAir(options.attachToAir())
                .useShift(options.useShift())
                .placementRate(options.placementRate())
                .autoRotate(options.autoRotate())
                .debugMode(options.debugMode())
                .build();
    }

    public record UpdateOptions(
            boolean enabled,
            double maxRange,
            ImmutableList<Integer> autoSelectSlots,
            boolean attachToAir,
            boolean useShift,
            double placementRate,
            boolean autoRotate,
            boolean debugMode
    ) {}

    public static class Builder {

        private boolean enabled;
        private double maxRange;
        private ImmutableList<Integer> autoSelectSlots;
        private boolean attachToAir;
        private boolean useShift;
        private double placementRate;
        private boolean autoRotate;
        private boolean debugMode;
        private String code;

        private Builder() {}

        public static Builder from(BlockAutomationConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .maxRange(config.maxRange)
                    .autoSelectSlots(config.autoSelectSlots)
                    .attachToAir(config.attachToAir)
                    .useShift(config.useShift)
                    .placementRate(config.placementRate)
                    .autoRotate(config.autoRotate)
                    .debugMode(config.debugMode)
                    .code(config.code);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder maxRange(double maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public Builder autoSelectSlots(ImmutableList<Integer> autoSelectSlots) {
            this.autoSelectSlots = autoSelectSlots;
            return this;
        }

        public Builder attachToAir(boolean attachToAir) {
            this.attachToAir = attachToAir;
            return this;
        }

        public Builder useShift(boolean useShift) {
            this.useShift = useShift;
            return this;
        }

        public Builder placementRate(double placementRate) {
            this.placementRate = placementRate;
            return this;
        }

        public Builder autoRotate(boolean autoRotate) {
            this.autoRotate = autoRotate;
            return this;
        }

        public Builder debugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public BlockAutomationConfig build() {
            return new BlockAutomationConfig(enabled, maxRange, autoSelectSlots, attachToAir, useShift, placementRate, autoRotate, debugMode, code);
        }
    }

    // </generated-code>
}