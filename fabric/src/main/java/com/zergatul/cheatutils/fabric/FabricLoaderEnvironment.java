package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricLoaderEnvironment implements LoaderEnvironment {

    public static final LoaderEnvironment INSTANCE = new FabricLoaderEnvironment();

    private FabricLoaderEnvironment() {}

    @Override
    public boolean isProduction() {
        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getLoaderName() {
        return "Fabric";
    }

    @Override
    public String getLoaderVersion() {
        return FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public int getModCount() {
        return FabricLoader.getInstance().getAllMods().size();
    }

    @Override
    public boolean hasMod(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public List<String> getModsJars() {
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