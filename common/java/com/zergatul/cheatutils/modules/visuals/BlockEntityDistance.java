package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.BlockEntityDistanceConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BlockEntityDistance implements Module {

    public static final BlockEntityDistance INSTANCE = new BlockEntityDistance();

    public static int VIEW_DISTANCE_CACHED = 64;
    private final ModuleConfigSettingHandle<BlockEntityDistanceConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().blockEntityDistance,
            config -> ConfigStore.instance.getConfig().blockEntityDistance = config);

    private BlockEntityDistance() {
        WebApiRegistry.INSTANCE.register("block-entity-distance", settings, BlockEntityDistanceConfig.class);
    }

    public ModuleConfigSettingHandle<BlockEntityDistanceConfig> getSettings() {
        return settings;
    }
}