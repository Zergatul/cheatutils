package com.zergatul.cheatutils.utils;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public final class Dimension {

    private static final Map<ResourceLocation, Dimension> dimensions = new HashMap<>();

    private final RegistryKey<World> key;
    private final DimensionType type;

    private Dimension(RegistryKey<World> key, DimensionType type) {
        this.key = key;
        this.type = type;
    }

    public static Dimension get(ClientWorld level) {
        Dimension existing = dimensions.get(level.dimension().location());
        if (existing != null) {
            return existing;
        }

        Dimension dimension = new Dimension(level.dimension(), level.dimensionType());
        dimensions.put(level.dimension().location(), dimension);
        return dimension;
    }

    @Override
    public int hashCode() {
        return key.location().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Dimension) {
            Dimension dimension = (Dimension) obj;
            return this.key == dimension.key;
        } else {
            return false;
        }
    }

    public ResourceLocation getId() {
        return key.location();
    }

    public int getMinY() {
        return 0;
    }

    public boolean hasCeiling() {
        return type.hasCeiling();
    }

    public int getLogicalHeight() {
        return type.logicalHeight();
    }

    public boolean isNether() {
        return key == World.NETHER;
    }
}