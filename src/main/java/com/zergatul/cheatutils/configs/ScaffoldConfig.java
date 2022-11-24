package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ScaffoldConfig implements ValidatableConfig {
    public boolean enabled;
    public double distance;
    public boolean replaceBlocksFromInventory;
    public boolean attachToAir;
    //public boolean useSlabs;

    @Override
    public void validate() {
        distance = MathUtils.clamp(distance, 0, 0.5);
    }
}