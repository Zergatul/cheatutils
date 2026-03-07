package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.modules.visuals.BlockEntityDistance;
import com.zergatul.cheatutils.utils.MathUtils;

public class BlockEntityDistanceConfig extends ModuleConfig implements Sanitizable {

    public int viewDistance;

    public BlockEntityDistanceConfig() {
        viewDistance = 64;
    }

    @Override
    public void sanitize() {
        viewDistance = MathUtils.clamp(viewDistance, 0, 65536);
        BlockEntityDistance.VIEW_DISTANCE_CACHED = enabled ? viewDistance : 64;
    }
}