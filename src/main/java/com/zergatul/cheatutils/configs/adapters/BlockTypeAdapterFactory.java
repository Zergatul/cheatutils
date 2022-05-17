package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.level.block.Block;

public class BlockTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (Block.class.isAssignableFrom(type.getRawType())) {
            return (TypeAdapter<T>) new BlockTypeAdapter();
        }
        return null;
    }
}
