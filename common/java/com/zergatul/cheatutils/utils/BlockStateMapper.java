package com.zergatul.cheatutils.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockStateMapper {

    private static final Pattern pattern = Pattern.compile("^(?<block>[a-z0-9_.-]+:[a-z0-9/._-]+)(?:\\[(?<properties>[a-z0-9_]+=[a-z0-9_]+(?:,[a-z0-9_]+=[a-z0-9_]+)*)\\])?$");

    public static BlockState map(String value) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            MatchResult result = matcher.toMatchResult();
            String id = result.group("block");

            Map<String, String> properties = new HashMap<>();
            String propertiesStr = result.group("properties");
            if (propertiesStr != null && !propertiesStr.isEmpty()) {
                for (String propertyStr : propertiesStr.split(",")) {
                    String[] parts = propertyStr.split("=");
                    if (parts.length != 2) {
                        throw new RuntimeException();
                    }
                    properties.put(parts[0], parts[1]);
                }
            }

            return map(id, properties);
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    public static BlockState map(CompoundTag compound) {
        return compound.getString("Name").map(id -> {
            if (id.contains("%%FILTER_ME%%")) {
                return Blocks.AIR.defaultBlockState();
            }

            Map<String, String> properties = new HashMap<>();
            compound.getCompound("Properties").ifPresent(tag -> {
                for (Map.Entry<String, Tag> entry : tag.entrySet()) {
                    properties.put(entry.getKey(), entry.getValue().asString().orElseThrow());
                }
            });

            return map(id, properties);
        }).orElse(Blocks.AIR.defaultBlockState());
    }

    public static BlockState map(String id, Map<String, String> properties) {
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));

        return block.getStateDefinition().getPossibleStates().stream().filter(state -> {
            return properties.entrySet().stream().allMatch(entry -> {
                String propertyName = entry.getKey();
                String value = entry.getValue();
                return state.getValues().anyMatch(e -> {
                    if (!propertyName.equals(e.property().getName())) {
                        return false;
                    }
                    return value.equals(e.valueName());
                });
            });
        }).findFirst().orElse(block.defaultBlockState());
    }

    public static CompoundTag serialize(BlockState state) {
        CompoundTag compound = new CompoundTag();
        compound.putString("Name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        Map<String, String> properties = getPropertiesAsStrings(state);
        if (!properties.isEmpty()) {
            compound.put("Properties", getPropertiesAsCompound(properties));
        }
        return compound;
    }

    public static String serializeAsString(BlockState state) {
        StringBuilder builder = new StringBuilder(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        Map<String, String> properties = getPropertiesAsStrings(state);
        if (!properties.isEmpty()) {
            builder.append('[');
            properties.keySet().stream().sorted().forEach(key -> builder
                    .append(key)
                    .append('=')
                    .append(properties.get(key))
                    .append(','));
            builder.delete(builder.length() - 1, builder.length());
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
        state.getValues().forEach(entry -> map.put(entry.property().getName(), entry.valueName()));

        return map;
    }
}