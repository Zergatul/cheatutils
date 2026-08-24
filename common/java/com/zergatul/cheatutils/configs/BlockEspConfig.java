package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.collections.ImmutableList;
import net.minecraft.world.level.block.Block;

import java.awt.*;

public class BlockEspConfig extends EspConfigBase {

    public ImmutableList<Block> blocks = new ImmutableList<>();

    public void copyFrom(BlockEspConfig jsonConfig) {
        copyFromJsonEspConfigBase(jsonConfig);
    }

    public static BlockEspConfig createDefault(ImmutableList<Block> blocks) {
        BlockEspConfig config = new BlockEspConfig();
        config.blocks = blocks;
        config.enabled = false;

        config.drawTracers = true;
        config.tracerColor = Color.WHITE;

        config.drawBoundingBox = true;
        config.boundingBoxColor = Color.WHITE;

        config.drawOverlay = false;
        config.overlayColor = new Color(0x80FFFFFF, true);

        config.maxDistance = DefaultMaxDistance;
        return config;
    }
}