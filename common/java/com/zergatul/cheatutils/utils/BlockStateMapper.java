package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

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
        Block block = Registries.BLOCKS.getValue(ResourceLocation.parse(id));

        return block.getStateDefinition().getPossibleStates().stream().filter(state -> {
            Map<Property<?>, Comparable<?>> currentProperties = state.getValues();
            return properties.entrySet().stream().allMatch(entry -> {
                String propertyName = entry.getKey();
                String value = entry.getValue();
                return currentProperties.entrySet().stream().anyMatch(e -> {
                    String currentPropertyName = e.getKey().getName();
                    if (!propertyName.equals(currentPropertyName)) {
                        return false;
                    }
                    String currentValue = getPropertyValueName(e.getKey(), e.getValue());
                    return value.equals(currentValue);
                });
            });
        }).findFirst().orElse(block.defaultBlockState());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> String getPropertyValueName(Property<T> property, Comparable<?> comparable) {
        return property.getName((T) comparable);
    }
}