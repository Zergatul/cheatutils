package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoBucketConfig implements ValidatableConfig {
    public boolean enabled;
    public double speedThreshold;
    public boolean useWaterBucket;
    public boolean useSlimeBlock;
    public boolean useCobweb;
    public boolean useHoneyBlock;
    public boolean useHayBale;
    public boolean autoMoveOnHotbar;
    public int hotbarSlot;

    public AutoBucketConfig() {
        hotbarSlot = 8;
        speedThreshold = 15;
    }

    @Override
    public void validate() {
        hotbarSlot = MathUtils.clamp(hotbarSlot, 0, 8);
        speedThreshold = MathUtils.clamp(speedThreshold, 0.1, 100);
    }
}