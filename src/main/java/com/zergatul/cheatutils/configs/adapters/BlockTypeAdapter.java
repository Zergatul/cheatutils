package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;

public class BlockTypeAdapter extends TypeAdapter<Block> {

    @Override
    public void write(JsonWriter out, Block block) throws IOException {
        if (block == null) {
            out.nullValue();
        } else {
            out.value(Registry.BLOCK.getKey(block).toString());
        }
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(value));
        }
    }
}
