package com.zergatul.cheatutils.utils;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;

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
}