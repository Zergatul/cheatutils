package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ItemTypeAdapter extends TypeAdapter<Item> {

    @Override
    public void write(JsonWriter out, Item item) throws IOException {
        if (item == null) {
            out.nullValue();
        } else {
            out.value(ModApiWrapper.ITEMS.getKey(item).toString());
        }
    }

    @Override
    public Item read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return ModApiWrapper.ITEMS.getValue(new ResourceLocation(value));
        }
    }
}