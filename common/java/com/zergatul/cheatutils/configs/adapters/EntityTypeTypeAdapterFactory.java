package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (EntityType.class.isAssignableFrom(type.getRawType())) {
            return (TypeAdapter<T>) new EntityTypeTypeAdapter().nullSafe();
        }
        return null;
    }
}