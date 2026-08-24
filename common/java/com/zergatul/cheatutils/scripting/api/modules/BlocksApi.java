package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Set;

@SuppressWarnings("unused")
public class BlocksApi {

    public boolean isEnabled(String blockId) {
        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return false;
        }
        return config.enabled;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String blockId) {
        BlockEspConfig config = getConfig(blockId);
        if (config == null) {
            return;
        }

        config.enabled = !config.enabled;
        ConfigStore.instance.requestWrite();
    }

    private BlockEspConfig getConfig(String blockId) {
        ResourceLocation location = new ResourceLocation(blockId);
        Block block = Registries.BLOCKS.getValue(location);
        if (block == null) {
            return null;
        }

        return ConfigStore.instance.getConfig().blocks.find(block);
    }

    @MethodDescription("""
            Returns blocks count which are tracked by Block ESP. If block is part of a group, returns count of entire group
            """)
    public int getCount(String blockId) {
        ResourceLocation location = ResourceLocation.tryParse(blockId);
        if (location == null) {
            return Integer.MAX_VALUE;
        }

        Block block = Registries.BLOCKS.getValue(location);
        if (block == null) {
            return Integer.MIN_VALUE;
        }

        BlockEspConfig config = ConfigStore.instance.getConfig().blocks.find(block);
        if (config == null) {
            return 0;
        }

        Set<BlockPos> set = BlockFinder.instance.blocks.get(config);
        if (set != null) {
            return set.size();
        } else {
            return 0;
        }
    }
}