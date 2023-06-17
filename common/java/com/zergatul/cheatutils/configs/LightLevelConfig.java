package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class LightLevelConfig extends ModuleConfig implements ValidatableConfig {

    public boolean showLocations;
    public boolean showTracers;
    public boolean showLightLevelValue;
    public boolean useFreeCamPosition;
    public float maxDistance;

    public LightLevelConfig() {
        enabled = false;
        showLocations = false;
        showTracers = true;
        maxDistance = 20;
    }

    @Override
    public void validate() {
        maxDistance = MathUtils.clamp(maxDistance, 1, 1000);
    }
}