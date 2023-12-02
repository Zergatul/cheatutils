package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ExplorationMiniMapConfig extends ModuleConfig implements ValidatableConfig {
    public Integer scanFromY;

    @Override
    public void validate() {
        if (scanFromY != null) {
            scanFromY = MathUtils.clamp(scanFromY, -1000, 1000);
        }
    }
}