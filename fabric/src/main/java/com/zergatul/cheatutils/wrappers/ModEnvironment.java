package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.fabricmc.loader.api.FabricLoader;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = !FabricLoader.getInstance().isDevelopmentEnvironment();

    public static String getModLoaderVersion() {
        return "Fabric: " + FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    public static String getModVersion() {
        return ModMain.MODID + ": " + FabricLoader.getInstance().getModContainer(ModMain.MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    public static String getModCount() {
        return "Mods: " + FabricLoader.getInstance().getAllMods().size();
    }
}