package com.zergatul.cheatutils.neoforge;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeVersion;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public final class NeoForgeLoaderEnvironment implements LoaderEnvironment {

    public static final LoaderEnvironment INSTANCE = new NeoForgeLoaderEnvironment();

    private NeoForgeLoaderEnvironment() {}

    @Override
    public boolean isProduction() {
        return FMLEnvironment.isProduction();
    }

    @Override
    public String getLoaderName() {
        return "NeoForge";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public String getLoaderVersion() {
        return NeoForgeVersion.getVersion();
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModFileById(Constants.MOD_ID).getMods().getFirst().getVersion().toString();
    }

    @Override
    public int getModCount() {
        return ModList.get().size();
    }

    @Override
    public boolean hasMod(String id) {
        return ModList.get().getModFileById(id) != null;
    }

    @Override
    public List<String> getModsJars() {
        return ModList.get().getModFiles().stream()
                .map(info -> info.getFile().getFilePath())
                .filter(path -> path.getFileSystem() == FileSystems.getDefault())
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                .map(path -> path.toAbsolutePath().normalize().toString())
                .distinct()
                .toList();
    }
}