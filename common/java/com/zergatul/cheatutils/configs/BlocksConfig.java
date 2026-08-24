package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.GsonSkip;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlocksConfig implements ModuleStateProvider, Sanitizable {

    private ImmutableList<BlockEspConfig> configs = new ImmutableList<>();

    @GsonSkip
    private volatile Map<Block, BlockEspConfig> map = Map.of();

    public ImmutableList<BlockEspConfig> getBlockConfigs() {
        return configs;
    }

    public Map<Block, BlockEspConfig> getMap() {
        return map;
    }

    public void apply() {
        refreshMap();
        BlockFinder.instance.removeAllConfigs();
        for (BlockEspConfig config: configs) {
            BlockFinder.instance.addConfig(config);
        }
    }

    public BlockEspConfig find(Block block) {
        return map.get(block);
    }

    public BlockEspConfig findExact(ImmutableList<Block> blocks) {
        for (BlockEspConfig config : configs) {
            if (config.blocks.equals(blocks)) {
                return config;
            }
        }

        return null;
    }

    public void add(BlockEspConfig config) {
        updateBlockConfigs(configs.add(config));
        BlockFinder.instance.addConfig(config);
    }

    public void remove(BlockEspConfig config) {
        updateBlockConfigs(configs.remove(config));
        BlockFinder.instance.removeConfig(config);
    }

    public void updateBlockConfigs(ImmutableList<BlockEspConfig> configs) {
        this.configs = configs;
        refreshMap();
    }

    public void refreshMap() {
        Map<Block, BlockEspConfig> map = new HashMap<>();
        for (BlockEspConfig config : configs) {
            for (Block block : config.blocks) {
                map.put(block, config);
            }
        }
        this.map = map;
    }

    @Override
    public boolean isEnabled() {
        return configs.stream().anyMatch(c -> c.enabled);
    }

    @Override
    public void sanitize() {
        if (configs == null) {
            configs = new ImmutableList<>();
        }

        Set<Block> assigned = new HashSet<>();
        ImmutableList<BlockEspConfig> sanitized = new ImmutableList<>();
        for (BlockEspConfig config : configs) {
            if (config == null) {
                continue;
            }

            config.sanitize();
            ImmutableList<Block> blocks = new ImmutableList<>();
            if (config.blocks != null) {
                for (Block block : config.blocks) {
                    if (block != null && block != Blocks.AIR && assigned.add(block)) {
                        blocks = blocks.add(block);
                    }
                }
            }

            if (!blocks.isEmpty()) {
                config.blocks = blocks;
                sanitized = sanitized.add(config);
            }
        }

        updateBlockConfigs(sanitized);
    }
}