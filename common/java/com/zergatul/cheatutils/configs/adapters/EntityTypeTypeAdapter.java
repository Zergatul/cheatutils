package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.io.IOException;

public class EntityTypeTypeAdapter extends TypeAdapter<EntityType<?>> {

    @Override
    public void write(JsonWriter out, EntityType<?> type) throws IOException {
        if (type == null) {
            out.nullValue();
        } else {
            out.value(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        }
    }

    @Override
    public EntityType<?> read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(value));
        }
    }
}