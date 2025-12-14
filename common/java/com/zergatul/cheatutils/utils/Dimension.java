package com.zergatul.cheatutils.utils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

public final class Dimension {

    private static Map<ResourceLocation, Dimension> dimensions = new HashMap<>();

    private final ResourceKey<Level> key;
    private final DimensionType type;

    private Dimension(ResourceKey<Level> key, DimensionType type) {
        this.key = key;
        this.type = type;
    }

    public static Dimension get(ClientLevel level) {
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
        if (obj instanceof Dimension dimension) {
            return this.key == dimension.key;
        } else {
            return false;
        }
    }

    public ResourceLocation getId() {
        return key.location();
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
        return key == Level.NETHER;
    }
}