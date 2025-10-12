package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.versions.forge.ForgeVersion;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = FMLEnvironment.production;
    public static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    public static String getModLoaderVersion() {
        return "Forge: " + ForgeVersion.getVersion();
    }

    public static String getModVersion() {
        return ModMain.MODID + ": " + ModList.get().getModFileById(ModMain.MODID).getMods().getFirst().getVersion().toString();
    }

    public static String getModCount() {
        return "Mods: " + ModList.get().size();
    }
}