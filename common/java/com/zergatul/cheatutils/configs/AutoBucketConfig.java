package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoBucketConfig extends ModuleConfig implements ValidatableConfig {
    public double speedThreshold;
    public boolean useWaterBucket;
    public boolean useSlimeBlock;
    public boolean useCobweb;
    public boolean useHoneyBlock;
    public boolean useHayBale;
    public boolean autoMoveOnHotbar;
    public int hotbarSlot;
    public boolean autoPickUp;
    public double reachDistance;

    public AutoBucketConfig() {
        hotbarSlot = 8;
        speedThreshold = 15;
        reachDistance = 4.5;
    }

    @Override
    public void validate() {
        hotbarSlot = MathUtils.clamp(hotbarSlot, 0, 8);
        speedThreshold = MathUtils.clamp(speedThreshold, 0.1, 100);
        reachDistance = MathUtils.clamp(reachDistance, 1, 20);
    }
}