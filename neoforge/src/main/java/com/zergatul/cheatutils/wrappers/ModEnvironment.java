package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = FMLEnvironment.isProduction();
    public static final boolean IRIS_LOADED = FMLLoader.getCurrent().getGameLayer().findModule("iris").isPresent(); //ModList.get().isLoaded("iris");

    public static String getModLoaderVersion() {
        return "NeoForge: " + NeoForgeVersion.getVersion();
    }

    public static String getModVersion() {
        return ModMain.MODID + ": " + ModList.get().getModFileById(ModMain.MODID).getMods().getFirst().getVersion().toString();
    }

    public static String getModCount() {
        return "Mods: " + ModList.get().size();
    }
}