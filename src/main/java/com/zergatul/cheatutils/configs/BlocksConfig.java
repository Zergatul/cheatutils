package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.controllers.BlockFinderController;

public class BlocksConfig {

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
}