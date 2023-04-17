package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ScaffoldConfig extends ModuleConfig implements ValidatableConfig {
    public double distance;
    public boolean replaceBlocksFromInventory;
    public boolean attachToAir;
    public boolean keepSelectedSlot;
    //public boolean useSlabs;

    @Override
    public void validate() {
        distance = MathUtils.clamp(distance, 0, 0.5);
    }
}