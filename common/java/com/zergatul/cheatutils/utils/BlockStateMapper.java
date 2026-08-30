package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockStateMapper {

    private static final Pattern pattern = Pattern.compile("^(?<block>[a-z0-9_.-]+:[a-z0-9/._-]+)(?:\\[(?<properties>[a-z0-9_]+=[a-z0-9_]+(?:,[a-z0-9_]+=[a-z0-9_]+)*)\\])?$");

    public static BlockState map(String value) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return Blocks.AIR.defaultBlockState();
        }

        MatchResult result = matcher.toMatchResult();
        Map<String, String> properties = new HashMap<>();
        String propertiesStr = result.group("properties");
        if (propertiesStr != null && !propertiesStr.isEmpty()) {
            for (String propertyStr : propertiesStr.split(",")) {
                String[] parts = propertyStr.split("=");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid block-state property: " + propertyStr);
                }
                properties.put(parts[0], parts[1]);
            }
        }

        return map(result.group("block"), properties);
    }

    public static BlockState map(CompoundTag compound) {
        String id = compound.getString("Name");
        if (id.contains("%%FILTER_ME%%")) {
            return Blocks.AIR.defaultBlockState();
        }

        Map<String, String> properties = new HashMap<>();
        if (compound.contains("Properties")) {
            CompoundTag propertiesTag = compound.getCompound("Properties");
            for (String key : propertiesTag.getAllKeys()) {
                properties.put(key, propertiesTag.getString(key));
            }
        }

        return map(id, properties);
    }

    public static BlockState map(String id, Map<String, String> properties) {
        Block block = Registries.BLOCKS.getValue(ResourceLocation.parse(id));
        return block.getStateDefinition().getPossibleStates().stream().filter(state -> {
            Map<Property<?>, Comparable<?>> currentProperties = state.getValues();
            return properties.entrySet().stream().allMatch(entry -> currentProperties.entrySet().stream().anyMatch(current -> {
                if (!entry.getKey().equals(current.getKey().getName())) {
                    return false;
                }
                return entry.getValue().equals(getPropertyValueName(current.getKey(), current.getValue()));
            }));
        }).findFirst().orElse(block.defaultBlockState());
    }

    public static CompoundTag serialize(BlockState state) {
        CompoundTag compound = new CompoundTag();
        compound.putString("Name", Registries.BLOCKS.getKey(state.getBlock()).toString());
        Map<String, String> properties = getPropertiesAsStrings(state);
        if (!properties.isEmpty()) {
            compound.put("Properties", getPropertiesAsCompound(properties));
        }
        return compound;
    }

    public static String serializeAsString(BlockState state) {
        StringBuilder builder = new StringBuilder(Registries.BLOCKS.getKey(state.getBlock()).toString());
        Map<String, String> properties = getPropertiesAsStrings(state);
        if (!properties.isEmpty()) {
            builder.append('[');
            properties.keySet().stream().sorted().forEach(key -> builder
                    .append(key)
                    .append('=')
                    .append(properties.get(key))
                    .append(','));
            builder.deleteCharAt(builder.length() - 1);
            builder.append(']');
        }
        return builder.toString();
    }

    private static CompoundTag getPropertiesAsCompound(Map<String, String> properties) {
        CompoundTag compound = new CompoundTag();
        properties.keySet().stream().sorted().forEach(key -> compound.putString(key, properties.get(key)));
        return compound;
    }

    private static Map<String, String> getPropertiesAsStrings(BlockState state) {
        if (state.getProperties().isEmpty()) {
            return Map.of();
        }

        Map<String, String> map = new HashMap<>(state.getProperties().size());
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            map.put(entry.getKey().getName(), getPropertyValueName(entry.getKey(), entry.getValue()));
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> String getPropertyValueName(Property<T> property, Comparable<?> comparable) {
        return property.getName((T) comparable);
    }
}