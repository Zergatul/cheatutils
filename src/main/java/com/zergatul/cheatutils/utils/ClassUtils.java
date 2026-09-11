package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;

public class ClassUtils {
    public static Class<?> forName(String name) throws ClassNotFoundException {
        // all mods and vanilla classes should be under the same class loader
        return Class.forName(name, false, Minecraft.class.getClassLoader());
    }
}