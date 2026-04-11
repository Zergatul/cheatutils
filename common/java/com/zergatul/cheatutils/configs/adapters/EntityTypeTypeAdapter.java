package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.common.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.io.IOException;

public class EntityTypeTypeAdapter extends TypeAdapter<EntityType<?>> {

    @Override
    public void write(JsonWriter out, EntityType<?> type) throws IOException {
        if (type == null) {
            out.nullValue();
        } else {
            out.value(Registries.ENTITY_TYPES.getKey(type).toString());
        }
    }

    @Override
    public EntityType<?> read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return Registries.ENTITY_TYPES.getValue(Identifier.parse(value));
        }
    }
}