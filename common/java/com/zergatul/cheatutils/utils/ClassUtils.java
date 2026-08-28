package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;

public class ClassUtils {

    public static Class<?> forName(String name) throws ClassNotFoundException {
        // Vanilla and mod classes are loaded by the same game class loader.
        return Class.forName(name, false, Minecraft.class.getClassLoader());
    }
}