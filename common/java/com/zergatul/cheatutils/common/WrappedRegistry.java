package com.zergatul.cheatutils.common;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface WrappedRegistry<T> {
    ResourceLocation getKey(T value);
    T getValue(ResourceLocation id) ;
    Collection<T> getValues();
}