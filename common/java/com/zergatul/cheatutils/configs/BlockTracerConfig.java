package com.zergatul.cheatutils.configs;

import net.minecraft.world.level.block.Block;

import java.awt.*;

public class BlockTracerConfig extends TracerConfigBase {

    public Block block;

    public boolean drawOverlay;
    public Color overlayColor;

    public void copyFrom(BlockTracerConfig jsonConfig) {
        copyFromJsonTracerConfigBase(jsonConfig);

        drawOverlay = jsonConfig.drawOverlay;
        overlayColor = jsonConfig.overlayColor;
    }

    public static BlockTracerConfig createDefault(Block block) {
        BlockTracerConfig config = new BlockTracerConfig();
        config.block = block;
        config.enabled = false;

        config.drawTracers = true;
        config.tracerColor = Color.WHITE;

        config.drawOutline = true;
        config.outlineColor = Color.WHITE;

        config.drawOverlay = false;
        config.overlayColor = new Color(0x80FFFFFF, true);

        config.maxDistance = DefaultMaxDistance;
        return config;
    }
}