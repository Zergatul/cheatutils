package com.zergatul.cheatutils.common;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class RegistryExtensions {

    public static <T> Collection<T> getValues(DefaultedRegistry<T> registry) {
        return registry.keySet().stream().map(registry::get).toList();
    }

    @Nullable
    public static <T> T safeParse(DefaultedRegistry<T> registry, String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return null;
        }

        return registry.get(location);
    }
}