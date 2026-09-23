package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public final class ForgeLoaderEnvironment implements LoaderEnvironment {

    public static final LoaderEnvironment INSTANCE = new ForgeLoaderEnvironment();

    private ForgeLoaderEnvironment() {}

    @Override
    public boolean isProduction() {
        return FMLEnvironment.production;
    }

    @Override
    public String getLoaderName() {
        return "Forge";
    }

    @Override
    public String getLoaderVersion() {
        return ForgeVersion.getVersion();
    }

    @Override
    public String getModVersion() {
        return ModList.getModFileById(Constants.MOD_ID).getMods().getFirst().getVersion().toString();
    }

    @Override
    public int getModCount() {
        return ModList.getMods().size();
    }

    @Override
    public boolean hasMod(String id) {
        return ModList.getModFileById(id) != null;
    }

    @Override
    public List<String> getModsJars() {
        return ModList.getModFiles().stream()
                .map(info -> info.getFile().getFilePath())
                .filter(path -> path.getFileSystem() == FileSystems.getDefault())
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                .map(path -> path.toAbsolutePath().normalize().toString())
                .distinct()
                .toList();
    }
}