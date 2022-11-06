package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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