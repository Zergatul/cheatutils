package com.zergatul.cheatutils.configs;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class JsonBlockTracerConfig extends JsonTracerConfigBase {

    public String blockId;

    public BlockTracerConfig convert() {
        BlockTracerConfig config = new BlockTracerConfig();
        config.block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        super.convert(config);
        return config;
    }
}
