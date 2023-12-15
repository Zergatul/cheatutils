package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.GsonSkip;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BlocksConfig {

    private ImmutableList<BlockEspConfig> configs = new ImmutableList<>();

    @GsonSkip
    private Map<Block, BlockEspConfig> map;

    public ImmutableList<BlockEspConfig> getBlockConfigs() {
        return configs;
    }

    public Map<Block, BlockEspConfig> getMap() {
        return map;
    }

    public void apply() {
        BlockFinderController.instance.removeAllConfigs();
        for (BlockEspConfig config: configs) {
            BlockFinderController.instance.addConfig(config);
        }
    }

    public BlockEspConfig find(Block block) {
        return map.get(block);
    }

    public BlockEspConfig findExact(ImmutableList<Block> blocks) {
        for (BlockEspConfig config: configs) {
            if (config.blocks.equals(blocks)) {
                return config;
            }
        }

        return null;
    }

    public void add(BlockEspConfig config) {
        updateBlockConfigs(configs.add(config));
        BlockFinderController.instance.addConfig(config);
    }

    public void remove(BlockEspConfig config) {
        updateBlockConfigs(configs.remove(config));
        BlockFinderController.instance.removeConfig(config);
    }

    public void updateBlockConfigs(ImmutableList<BlockEspConfig> configs) {
        Map<Block, BlockEspConfig> map = rebuildMap(configs);
        this.configs = configs;
        this.map = map;
    }

    public void refreshMap() {
        map = rebuildMap(configs);
    }

    private Map<Block, BlockEspConfig> rebuildMap(ImmutableList<BlockEspConfig> configs) {
        Map<Block, BlockEspConfig> map = new HashMap<>();
        for (BlockEspConfig config: configs) {
            for (Block block: config.blocks) {
                map.put(block, config);
            }
        }
        return map;
    }
}