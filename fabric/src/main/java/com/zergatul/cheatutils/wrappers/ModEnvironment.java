package com.zergatul.cheatutils.wrappers;

import net.fabricmc.loader.api.FabricLoader;

public class ModEnvironment {
    public static final boolean isProduction = !FabricLoader.getInstance().isDevelopmentEnvironment();
}