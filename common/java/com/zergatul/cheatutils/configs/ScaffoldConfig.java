package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ScaffoldConfig extends ModuleConfig implements Sanitizable {
    public double distance;
    public boolean replaceBlocksFromInventory;
    public boolean attachToAir;
    public boolean keepSelectedSlot;

    @Override
    public void sanitize() {
        distance = MathUtils.clamp(distance, 0, 0.5);
    }
}