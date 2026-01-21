package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeVersion;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = FMLEnvironment.isProduction();

    public static String getModLoader() {
        return "NeoForge";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static String getModLoaderVersion() {
        return NeoForgeVersion.getVersion();
    }

    public static String getModVersion() {
        return ModList.get().getModFileById(ModMain.MODID).getMods().getFirst().getVersion().toString();
    }

    public static int getModCount() {
        return ModList.get().size();
    }
}