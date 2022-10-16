package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class BlockTypeAdapter extends TypeAdapter<Block> {

    @Override
    public void write(JsonWriter out, Block block) throws IOException {
        if (block == null) {
            out.nullValue();
        } else {
            out.value(ModApiWrapper.BLOCKS.getKey(block).toString());
        }
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return ModApiWrapper.BLOCKS.getValue(new ResourceLocation(value));
        }
    }
}
