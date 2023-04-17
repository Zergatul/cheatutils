package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.common.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;

public class BlockStateTypeAdapter extends TypeAdapter<BlockState> {

    @Override
    public void write(JsonWriter out, BlockState state) throws IOException {
        if (state == null) {
            out.nullValue();
        } else {
            out.value(Registries.BLOCKS.getKey(state.getBlock()).toString());
        }
    }

    @Override
    public BlockState read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return Registries.BLOCKS.getValue(new ResourceLocation(value)).defaultBlockState();
        }
    }
}