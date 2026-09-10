package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.utils.ResourceLocationHelper;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.IOException;

public class BlockTypeAdapter extends TypeAdapter<Block> {

    @Override
    public void write(JsonWriter out, Block block) throws IOException {
        if (block == null) {
            out.nullValue();
        } else {
            ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
            if (location != null) {
                out.value(location.toString());
            } else {
                out.nullValue();
            }
        }
    }

    @Override
    public Block read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            ResourceLocation location = ResourceLocationHelper.parseSafe(value);
            if (location == null) {
                return null;
            }
            return ForgeRegistries.BLOCKS.getValue(location);
        }
    }
}