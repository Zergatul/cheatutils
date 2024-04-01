package com.zergatul.cheatutils.common;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface WrappedRegistry<T> {
    ResourceLocation getKey(T value);
    T getValue(ResourceLocation id) ;
    Collection<T> getValues();

    default T safeParse(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return null;
        }

        return getValue(location);
    }
}