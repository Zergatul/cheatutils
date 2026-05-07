package com.zergatul.cheatutils.configs;

import net.minecraft.world.level.block.Blocks;

public class BedrockBreakerConfig {

    public boolean replace;
    public String replaceBlockId;
    public boolean allBlocks;

    public BedrockBreakerConfig() {
        // this doesn't work since config init happens very early before ModMain, at least in Forge
        // Registries.ITEMS.getKey(Items.NETHERRACK).toString();
        replaceBlockId = Blocks.NETHERRACK.builtInRegistryHolder().key().identifier().toString();
    }
}