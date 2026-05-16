package com.zergatul.cheatutils.wrappers;

import net.fabricmc.loader.api.FabricLoader;

public class ModEnvironment {

    public static final boolean isProduction = !FabricLoader.getInstance().isDevelopmentEnvironment();

    public static boolean hasMod(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}