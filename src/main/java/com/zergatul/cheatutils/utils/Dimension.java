package com.zergatul.cheatutils.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

public final class Dimension {

    private static Map<Identifier, Dimension> dimensions = new HashMap<>();

    private final RegistryKey<World> key;
    private final DimensionType type;

    private Dimension(RegistryKey<World> key, DimensionType type) {
        this.key = key;
        this.type = type;
    }

    public static Dimension get(ClientWorld level) {
        Dimension existing = dimensions.get(level.getDimensionKey().getValue());
        if (existing != null) {
            return existing;
        }

        Dimension dimension = new Dimension(level.getRegistryKey(), level.getDimension());
        dimensions.put(level.getDimensionKey().getValue(), dimension);
        return dimension;
    }

    @Override
    public int hashCode() {
        return key.getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Dimension dimension) {
            return this.key == dimension.key;
        } else {
            return false;
        }
    }

    public Identifier getId() {
        return key.getValue();
    }

    public int getMinY() {
        return type.minY();
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