package com.zergatul.cheatutils.common;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class RegistryExtensions {

    public static final Block DEFAULT_BLOCK = RegistryExtensions.getDefault(BuiltInRegistries.BLOCK);

    public static <T> T getDefault(DefaultedRegistry<T> registry) {
        return registry.getValue(registry.getDefaultKey());
    }

    public static <T> Collection<T> getValues(DefaultedRegistry<T> registry) {
        return registry.keySet().stream().map(registry::getValue).toList();
    }

    @Nullable
    public static <T> T safeParse(DefaultedRegistry<T> registry, String id) {
        Identifier location = Identifier.tryParse(id);
        if (location == null) {
            return null;
        }

        if (location.equals(registry.getDefaultKey())) {
            return null;
        }

        T value = registry.getValue(location);
        if (registry.getKey(value).equals(registry.getDefaultKey())) {
            return null;
        }

        return value;
    }

    public static <T> T strictParse(DefaultedRegistry<T> registry, String id) {
        Identifier location = Identifier.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException(id + " is not a valid identifier.");
        }

        if (location.equals(registry.getDefaultKey())) {
            throw new IllegalArgumentException(id + " is default identifier.");
        }

        T value = registry.getValue(location);
        if (registry.getKey(value).equals(registry.getDefaultKey())) {
            throw new IllegalArgumentException(id + " resolves to default value.");
        }

        return value;
    }
}