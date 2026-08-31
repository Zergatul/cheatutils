package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.utils.BlockStateMapper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockStateTypeAdapter extends TypeAdapter<BlockState> {

    private static final String BLOCK_PROPERTY = "block";
    private static final String PROPERTIES_PROPERTY = "properties";

    @Override
    public void write(JsonWriter out, BlockState state) throws IOException {
        if (state == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name(BLOCK_PROPERTY).value(Registries.BLOCKS.getKey(state.getBlock()).toString());
            if (!state.getValues().isEmpty()) {
                out.name(PROPERTIES_PROPERTY).beginObject();
                for (var entry : state.getValues().entrySet()) {
                    Property<?> property = entry.getKey();
                    out.name(property.getName()).value(getName(property, entry.getValue()));
                }
                out.endObject();
            }
            out.endObject();
        }
    }

    @Override
    public BlockState read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (in.peek() == JsonToken.STRING) {
            return BlockStateMapper.map(in.nextString());
        }

        String id = null;
        Map<String, String> properties = new HashMap<>();
        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            if (name.equals(BLOCK_PROPERTY)) {
                id = in.nextString();
            } else if (name.equals(PROPERTIES_PROPERTY)) {
                in.beginObject();
                while (in.peek() != JsonToken.END_OBJECT) {
                    properties.put(in.nextName(), in.nextString());
                }
                in.endObject();
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        if (id == null) {
            throw new IOException("Block id not found.");
        }
        return BlockStateMapper.map(id, properties);
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName((T) comparable);
    }
}