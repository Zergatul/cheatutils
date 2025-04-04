package com.zergatul.cheatutils.utils;

import net.minecraft.nbt.*;

import java.util.Optional;

public class NbtUtils {

    public static boolean hasShort(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof ShortTag;
    }

    public static boolean hasInt(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof IntTag;
    }

    public static boolean hasBytes(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof ByteArrayTag;
    }

    public static boolean hasInts(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof IntArrayTag;
    }

    public static boolean hasLongs(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof LongArrayTag;
    }

    public static boolean hasCompound(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof CompoundTag;
    }

    public static boolean hasString(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof StringTag;
    }

    public static boolean hasList(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof ListTag;
    }

    public static Optional<String> getString(CompoundTag compound, String key) {
        if (compound.contains(key, Tag.TAG_STRING)) {
            return Optional.of(compound.getString(key));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<CompoundTag> getCompound(CompoundTag compound, String key) {
        if (compound.contains(key, Tag.TAG_COMPOUND)) {
            return Optional.of(compound.getCompound(key));
        } else {
            return Optional.empty();
        }
    }
}