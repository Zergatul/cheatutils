package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.mixins.common.accessors.MinecraftAccessor;
import net.minecraft.client.Minecraft;

public class ClientTicks {
    public static long get() {
        return ((MinecraftAccessor) Minecraft.getInstance()).getClientTickCount_CU();
    }
}