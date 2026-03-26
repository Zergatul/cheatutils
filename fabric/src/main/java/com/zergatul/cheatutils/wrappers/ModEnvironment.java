package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.utils.ResourceHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;

public class ModEnvironment {

    public static final boolean IS_PRODUCTION = !FabricLoader.getInstance().isDevelopmentEnvironment();
    public static final boolean IS_CURSEFORGE_RESTRICTED = isCurseForgeRestricted();

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

    private static boolean isCurseForgeRestricted() {
        InputStream stream = ResourceHelper.get("curse-forge-build");
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException _) {}
            return true;
        } else {
            return false;
        }
    }
}