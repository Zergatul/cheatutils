package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class BlocksApi {

    public boolean isEnabled(String blockId) {
        var config = getConfig(blockId);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String blockId) {
        var config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    public int getCount(String blockId) {
        ResourceLocation location = new ResourceLocation(blockId);
        Block block = Registries.BLOCKS.getValue(location);
        if (block == null) {
            return Integer.MIN_VALUE;
        }

        BlockEspConfig config = ConfigStore.instance.getConfig().blocks.find(block);
        if (config == null) {
            return 0;
        }

        Set<BlockPos> set = BlockFinder.instance.blocks.get(config);
        if (set == null) {
            return 0;
        } else {
            return set.size();
        }
    }

    private BlockEspConfig getConfig(String blockId) {
        ResourceLocation location = new ResourceLocation(blockId);
        Block block = Registries.BLOCKS.getValue(location);
        if (block == null) {
            return null;
        }

        return ConfigStore.instance.getConfig().blocks.find(block);
    }
}