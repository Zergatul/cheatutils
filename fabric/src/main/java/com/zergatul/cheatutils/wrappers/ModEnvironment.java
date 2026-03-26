package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.fabricmc.loader.api.FabricLoader;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = !FabricLoader.getInstance().isDevelopmentEnvironment();

    public static String getModLoader() {
        return "Fabric";
    }

    public static String getModLoaderVersion() {
        return FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    public static String getModVersion() {
        return FabricLoader.getInstance().getModContainer(ModMain.MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    public static int getModCount() {
        return FabricLoader.getInstance().getAllMods().size();
    }
}