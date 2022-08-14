package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;

public class ItemTypeAdapter extends TypeAdapter<Item> {

    @Override
    public void write(JsonWriter out, Item item) throws IOException {
        if (item == null) {
            out.nullValue();
        } else {
            out.value(ForgeRegistries.ITEMS.getKey(item).toString());
        }
    }

    @Override
    public Item read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(value));
        }
    }
}