package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.mixins.common.accessors.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public class KeyUtils {

    public static void click(KeyMapping mapping) {
        KeyMapping.click(((KeyMappingAccessor) mapping).getKey_CU());
    }
}