package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public static List<String> getModsJars() {
        List<String> result = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) {
                continue;
            }

            for (Path path : mod.getOrigin().getPaths()) {
                if (path.toString().endsWith(".jar")) {
                    result.add(path.toString());
                }
            }
        }
        return result;
    }
}