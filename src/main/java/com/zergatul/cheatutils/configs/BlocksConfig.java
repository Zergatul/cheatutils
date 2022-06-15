package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.controllers.BlockFinderController;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BlocksConfig {

    public List<BlockTracerConfig> configs = new ArrayList<>();

    public void apply() {
        synchronized (BlockFinderController.instance.blocks) {
            BlockFinderController.instance.blocks.clear();
            for (BlockTracerConfig config: configs) {
                BlockFinderController.instance.blocks.put(Registry.BLOCK.getKey(config.block), new HashSet<>());
            }
        }
    }

    public void add(BlockTracerConfig config) {
        synchronized (configs) {
            configs.add(config);
        }
        synchronized (BlockFinderController.instance.blocks) {
            BlockFinderController.instance.blocks.put(Registry.BLOCK.getKey(config.block), new HashSet<>());
        }
    }

    public void remove(BlockTracerConfig config) {
        synchronized (configs) {
            configs.remove(config);
        }
        synchronized (BlockFinderController.instance.blocks) {
            BlockFinderController.instance.blocks.remove(Registry.BLOCK.getKey(config.block));
        }
    }
}