package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.BlockEntityDistanceConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BlockEntityDistance implements Module {

    public static final BlockEntityDistance INSTANCE = new BlockEntityDistance();
    public static int VIEW_DISTANCE_CACHED = 64;

    private final SettingHandle2<BlockEntityDistanceConfig> settings = Settings.get(BlockEntityDistanceConfig.class);

    private BlockEntityDistance() {
        settings.onChanged(this::onConfigChanged);
        WebApiRegistry.INSTANCE.register("block-entity-distance", settings, BlockEntityDistanceConfig.class);
    }

    public SettingHandle2<BlockEntityDistanceConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(BlockEntityDistanceConfig prev, BlockEntityDistanceConfig curr) {
        VIEW_DISTANCE_CACHED = curr.enabled() ? curr.viewDistance() : 64;
    }
}