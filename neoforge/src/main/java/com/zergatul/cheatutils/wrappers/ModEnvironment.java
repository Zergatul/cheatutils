package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class ModEnvironment {

    public static final boolean isProduction = FMLEnvironment.isProduction();

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