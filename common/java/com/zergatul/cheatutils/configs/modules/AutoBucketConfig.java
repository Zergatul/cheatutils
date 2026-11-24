package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AutoBucketConfig(
        boolean enabled,
        double speedThreshold,
        boolean useWaterBucket,
        boolean usePowderSnowBucket,
        boolean useSlimeBlock,
        boolean useHoneyBlock,
        boolean useCobweb,
        boolean useHayBale,
        boolean autoMoveOnHotbar,
        int hotbarSlot,
        boolean autoPickUp,
        double reachDistance
) implements ImmutableConfig, ToggleableConfig<AutoBucketConfig> {

    public AutoBucketConfig {
        hotbarSlot = MathUtils.clamp(hotbarSlot, 0, 8);
        speedThreshold = MathUtils.clamp(speedThreshold, 0.1, 100);
        reachDistance = MathUtils.clamp(reachDistance, 1, 20);
    }

    public static AutoBucketConfig defaults() {
        return new Builder()
                .enabled(false)
                .speedThreshold(15)
                .useWaterBucket(false)
                .usePowderSnowBucket(false)
                .useSlimeBlock(false)
                .useHoneyBlock(false)
                .useCobweb(false)
                .useHayBale(false)
                .autoMoveOnHotbar(false)
                .hotbarSlot(8)
                .autoPickUp(false)
                .reachDistance(4.5)
                .build();
    }

    // <generated-code>

    public AutoBucketConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public AutoBucketConfig withSpeedThreshold(double speedThreshold) {
        return Builder.from(this).speedThreshold(speedThreshold).build();
    }

    public AutoBucketConfig withUseWaterBucket(boolean useWaterBucket) {
        return Builder.from(this).useWaterBucket(useWaterBucket).build();
    }

    public AutoBucketConfig withUsePowderSnowBucket(boolean usePowderSnowBucket) {
        return Builder.from(this).usePowderSnowBucket(usePowderSnowBucket).build();
    }

    public AutoBucketConfig withUseSlimeBlock(boolean useSlimeBlock) {
        return Builder.from(this).useSlimeBlock(useSlimeBlock).build();
    }

    public AutoBucketConfig withUseHoneyBlock(boolean useHoneyBlock) {
        return Builder.from(this).useHoneyBlock(useHoneyBlock).build();
    }

    public AutoBucketConfig withUseCobweb(boolean useCobweb) {
        return Builder.from(this).useCobweb(useCobweb).build();
    }

    public AutoBucketConfig withUseHayBale(boolean useHayBale) {
        return Builder.from(this).useHayBale(useHayBale).build();
    }

    public AutoBucketConfig withAutoMoveOnHotbar(boolean autoMoveOnHotbar) {
        return Builder.from(this).autoMoveOnHotbar(autoMoveOnHotbar).build();
    }

    public AutoBucketConfig withHotbarSlot(int hotbarSlot) {
        return Builder.from(this).hotbarSlot(hotbarSlot).build();
    }

    public AutoBucketConfig withAutoPickUp(boolean autoPickUp) {
        return Builder.from(this).autoPickUp(autoPickUp).build();
    }

    public AutoBucketConfig withReachDistance(double reachDistance) {
        return Builder.from(this).reachDistance(reachDistance).build();
    }

    public static class Builder {

        private boolean enabled;
        private double speedThreshold;
        private boolean useWaterBucket;
        private boolean usePowderSnowBucket;
        private boolean useSlimeBlock;
        private boolean useHoneyBlock;
        private boolean useCobweb;
        private boolean useHayBale;
        private boolean autoMoveOnHotbar;
        private int hotbarSlot;
        private boolean autoPickUp;
        private double reachDistance;

        private Builder() {}

        public static Builder from(AutoBucketConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .speedThreshold(config.speedThreshold)
                    .useWaterBucket(config.useWaterBucket)
                    .usePowderSnowBucket(config.usePowderSnowBucket)
                    .useSlimeBlock(config.useSlimeBlock)
                    .useHoneyBlock(config.useHoneyBlock)
                    .useCobweb(config.useCobweb)
                    .useHayBale(config.useHayBale)
                    .autoMoveOnHotbar(config.autoMoveOnHotbar)
                    .hotbarSlot(config.hotbarSlot)
                    .autoPickUp(config.autoPickUp)
                    .reachDistance(config.reachDistance);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder speedThreshold(double speedThreshold) {
            this.speedThreshold = speedThreshold;
            return this;
        }

        public Builder useWaterBucket(boolean useWaterBucket) {
            this.useWaterBucket = useWaterBucket;
            return this;
        }

        public Builder usePowderSnowBucket(boolean usePowderSnowBucket) {
            this.usePowderSnowBucket = usePowderSnowBucket;
            return this;
        }

        public Builder useSlimeBlock(boolean useSlimeBlock) {
            this.useSlimeBlock = useSlimeBlock;
            return this;
        }

        public Builder useHoneyBlock(boolean useHoneyBlock) {
            this.useHoneyBlock = useHoneyBlock;
            return this;
        }

        public Builder useCobweb(boolean useCobweb) {
            this.useCobweb = useCobweb;
            return this;
        }

        public Builder useHayBale(boolean useHayBale) {
            this.useHayBale = useHayBale;
            return this;
        }

        public Builder autoMoveOnHotbar(boolean autoMoveOnHotbar) {
            this.autoMoveOnHotbar = autoMoveOnHotbar;
            return this;
        }

        public Builder hotbarSlot(int hotbarSlot) {
            this.hotbarSlot = hotbarSlot;
            return this;
        }

        public Builder autoPickUp(boolean autoPickUp) {
            this.autoPickUp = autoPickUp;
            return this;
        }

        public Builder reachDistance(double reachDistance) {
            this.reachDistance = reachDistance;
            return this;
        }

        public AutoBucketConfig build() {
            return new AutoBucketConfig(enabled, speedThreshold, useWaterBucket, usePowderSnowBucket, useSlimeBlock, useHoneyBlock, useCobweb, useHayBale, autoMoveOnHotbar, hotbarSlot, autoPickUp, reachDistance);
        }
    }

    // </generated-code>
}