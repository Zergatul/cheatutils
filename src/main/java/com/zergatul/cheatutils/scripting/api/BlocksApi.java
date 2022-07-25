package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class BlocksApi {

    public boolean isEnabled(String blockId) {
        var config = getConfig(blockId);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    public void toggle(String blockId) {
        var config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private BlockTracerConfig getConfig(String blockId) {
        ResourceLocation location = new ResourceLocation(blockId);
        Block block = ForgeRegistries.BLOCKS.getValue(location);
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