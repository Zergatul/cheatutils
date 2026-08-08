package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;

public class BlocksConfig implements ModuleStateProvider, Sanitizable {

    public ImmutableList<BlockTracerConfig> configs = new ImmutableList<>();

    public void apply() {
        BlockFinderController.instance.removeAllConfigs();
        for (BlockTracerConfig config: configs) {
            BlockFinderController.instance.addConfig(config);
        }
    }

    public void add(BlockTracerConfig config) {
        configs = configs.add(config);
        BlockFinderController.instance.addConfig(config);
    }

    public void remove(BlockTracerConfig config) {
        configs = configs.remove(config);
        BlockFinderController.instance.removeConfig(config);
    }

    @Override
    public boolean isEnabled() {
        return configs.stream().anyMatch(c -> c.enabled);
    }

    @Override
    public void sanitize() {
        configs = configs
                .removeIf(Objects::isNull)
                .removeIf(c -> c.block == null)
                .removeIf(c -> c.block == Blocks.AIR);
    }
}