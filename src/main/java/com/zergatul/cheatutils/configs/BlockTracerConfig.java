package com.zergatul.cheatutils.configs;

import net.minecraft.world.level.block.Block;

import java.awt.*;

public class BlockTracerConfig extends TracerConfigBase {

    public Block block;

    public JsonBlockTracerConfig convert() {
        JsonBlockTracerConfig config = new JsonBlockTracerConfig();
        config.blockId = block.getRegistryName().toString();
        super.convert(config);
        return config;
    }

    public void copyFrom(JsonBlockTracerConfig jsonConfig) {
        copyFromJsonTracerConfigBase(jsonConfig);
    }

    public static BlockTracerConfig createDefault(Block block) {
        BlockTracerConfig config = new BlockTracerConfig();
        config.block = block;
        config.enabled = false;
        config.drawTracers = true;
        config.tracerColor = Color.WHITE;
        config.tracerLineWidth = 2;
        config.tracerLineStyle = 0;
        config.drawOutline = true;
        config.outlineColor = Color.WHITE;
        config.outlineLineWidth = 2;
        config.maxDistance = DefaultMaxDistance;
        return config;
    }
}
