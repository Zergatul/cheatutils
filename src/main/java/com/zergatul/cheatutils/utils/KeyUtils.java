package com.zergatul.cheatutils.utils;

import net.minecraft.client.KeyMapping;

public class KeyUtils {

    public static void click(KeyMapping mapping) {
        KeyMapping.click(mapping.getKey());
    }
}