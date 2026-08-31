package com.zergatul.cheatutils.utils;

import net.minecraft.nbt.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class NbtUtils {

    public static CompoundTag readCompressed(byte[] data, long maxBytes) throws IOException {
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(new ByteArrayInputStream(data))))
        ) {
            return NbtIo.read(stream, new NbtAccounter(maxBytes));
        }
    }

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
}