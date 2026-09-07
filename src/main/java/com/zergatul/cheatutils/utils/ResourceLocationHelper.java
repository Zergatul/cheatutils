package com.zergatul.cheatutils.utils;

import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class ResourceLocationHelper {

    public static @Nullable ResourceLocation parseSafe(String value) {
        try {
            return new ResourceLocation(value);
        } catch (Throwable ex) {
            return null;
        }
    }
}