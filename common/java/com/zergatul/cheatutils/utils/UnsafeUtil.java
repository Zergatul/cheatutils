package com.zergatul.cheatutils.utils;

import org.lwjgl.system.MemoryUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {

    public static Unsafe get() {
        Field field;
        try {
            field = MemoryUtil.class.getDeclaredField("UNSAFE");
        }
        catch (NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find UNSAFE field.");
        }

        field.setAccessible(true);

        try {
            return (Unsafe) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot get UNSAFE field.");
        }
    }
}