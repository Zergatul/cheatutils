package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.ModMain;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

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

    public static List<String> getModsJars() {
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