package com.zergatul.cheatutils.wrappers;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ModEnvironment {

    public static final boolean isProduction = FMLEnvironment.production;

    public static boolean hasMod(String modId) {
        return ModList.get().getModFileById(modId) != null;
    }
}