package com.zergatul.cheatutils.wrappers;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class WrapperRegistry<T> {

    private final Registry<T> registry;

    public WrapperRegistry(Registry<T> registry) {
        this.registry = registry;
    }

    public Identifier getKey(T value) {
        Optional<RegistryKey<T>> optional = registry.getKey(value);
        if (optional.isEmpty()) {
            return null;
        }

        return optional.get().getValue();
    }

    public T getValue(Identifier id) {
        return registry.get(id);
    }

    public Collection<T> getValues() {
        return registry.getEntrySet().stream().map(Map.Entry::getValue).toList();
    }
}