package com.zergatul.cheatutils.tests.utility;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@NullMarked
public class MockRegistryAccess implements RegistryAccess {

    private final RegistryAccess inner;

    public MockRegistryAccess() {
        HolderLookup.Provider provider = VanillaRegistries.createLookup();
        inner = new RegistryAccess.ImmutableRegistryAccess(List.of(
                copyRegistry(provider, Registries.BIOME),
                copyRegistry(provider, Registries.DAMAGE_TYPE),
                copyRegistry(provider, Registries.DIMENSION_TYPE)));
    }

    @Override
    public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return inner.lookup(registryKey);
    }

    @Override
    public Stream<RegistryEntry<?>> registries() {
        return inner.registries();
    }

    private static <T> Registry<T> copyRegistry(HolderLookup.Provider provider, ResourceKey<? extends Registry<T>> key) {
        HolderLookup.RegistryLookup<T> lookup = provider.lookupOrThrow(key);
        MappedRegistry<T> registry = new MappedRegistry<>(key, Lifecycle.stable());
        lookup.listElements().forEach(holder -> registry.register(holder.key(), holder.value(), RegistrationInfo.BUILT_IN));
        return registry.freeze();
    }
}