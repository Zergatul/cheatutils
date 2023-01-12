package com.zergatul.cheatutils.utils;

import net.minecraft.nbt.*;

public class NbtUtils {

    public static boolean hasShort(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof ShortTag;
    }

    public static boolean hasBytes(CompoundTag compound, String key) {
        Tag value = compound.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof ByteArrayTag;
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
}