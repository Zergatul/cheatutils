package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlocksApi {

    public boolean isEnabled(String blockId) {
        var config = getConfig(blockId);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    private BlockTracerConfig getConfig(String blockId) {
        ResourceLocation location = new ResourceLocation(blockId);
        Block block = ModApiWrapper.BLOCKS.getValue(location);
        if (block == null) {
            return null;
        }

        BlockTracerConfig config;
        var list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            return list.stream().filter(c -> c.block == block).findFirst().orElse(null);
        }
    }
}