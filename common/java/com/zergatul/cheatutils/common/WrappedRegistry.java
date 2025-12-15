package com.zergatul.cheatutils.common;

import net.minecraft.resources.Identifier;

import java.util.Collection;

public interface WrappedRegistry<T> {
    Identifier getKey(T value);
    T getValue(Identifier id) ;
    Collection<T> getValues();

    default T safeParse(String id) {
        Identifier location = Identifier.tryParse(id);
        if (location == null) {
            return null;
        }

        return getValue(location);
    }
}