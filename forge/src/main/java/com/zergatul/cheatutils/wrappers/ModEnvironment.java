package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.versions.forge.ForgeVersion;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = FMLEnvironment.production;

    public static String getModLoader() {
        return "Forge";
    }

    public static String getModLoaderVersion() {
        return ForgeVersion.getVersion();
    }

    public static String getModVersion() {
        return ModList.get().getModFileById(ModMain.MODID).getMods().getFirst().getVersion().toString();
    }

    public static int getModCount() {
        return ModList.get().size();
    }
}