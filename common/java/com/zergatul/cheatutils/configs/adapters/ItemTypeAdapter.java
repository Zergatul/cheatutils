package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.io.IOException;

public class ItemTypeAdapter extends TypeAdapter<Item> {

    @Override
    public void write(JsonWriter out, Item item) throws IOException {
        if (item == null) {
            out.nullValue();
        } else {
            out.value(BuiltInRegistries.ITEM.getKey(item).toString());
        }
    }

    @Override
    public Item read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return BuiltInRegistries.ITEM.getValue(Identifier.parse(value));
        }
    }
}