package com.zergatul.cheatutils.wrappers;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

public class ModEnvironment {

    public static final boolean isProduction = FMLEnvironment.production;

    public static boolean hasMod(String modId) {
        return ModList.get().getModFileById(modId) != null;
    }
}