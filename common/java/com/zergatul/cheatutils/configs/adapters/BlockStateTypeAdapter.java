package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.utils.BlockStateMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

            out.name(BLOCK_PROPERTY);
            out.value(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());

            List<Property.Value<?>> values = state.getValues().toList();
            if (!values.isEmpty()) {
                out.name(PROPERTIES_PROPERTY);
                out.beginObject();

                for (Property.Value<?> entry : values) {
                    out.name(entry.property().getName());
                    out.value(entry.valueName());
                }

                out.endObject();
            }

            out.endObject();
        }
    }

    @Override
    public BlockState read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return null;
        }

        String id = null;
        Map<String, String> properties = new HashMap<>();

        in.beginObject();
        while (true) {
            if (in.peek() == JsonToken.END_OBJECT) {
                in.endObject();
                break;
            }

            String name = in.nextName();
            if (name.equals(BLOCK_PROPERTY)) {
                id = in.nextString();
            }
            if (name.equals(PROPERTIES_PROPERTY)) {
                in.beginObject();
                while (true) {
                    if (in.peek() == JsonToken.END_OBJECT) {
                        in.endObject();
                        break;
                    }
                    properties.put(in.nextName(), in.nextString());
                }
            }
        }

        if (id == null) {
            throw new IOException("Block id not found.");
        }

        return BlockStateMapper.map(id, properties);
    }
}