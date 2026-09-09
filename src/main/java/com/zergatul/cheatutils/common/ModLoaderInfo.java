package com.zergatul.cheatutils.common;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModLoaderInfo {

    public static final ModLoaderInfo INSTANCE = new ModLoaderInfo();

    private ModLoaderInfo() {}

    public boolean isProduction() {
        return !FMLLaunchHandler.isDeobfuscatedEnvironment();
    }

    public List<String> getModsJars() {
        return Loader.instance().getActiveModList().stream()
                .map(ModContainer::getSource)
                .filter(Objects::nonNull)
                .map(File::toPath)
                .filter(path -> path.getFileSystem() == FileSystems.getDefault())
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString()
                        .toLowerCase(Locale.ROOT)
                        .endsWith(".jar"))
                .map(path -> path.toAbsolutePath().normalize().toString())
                .distinct()
                .collect(Collectors.toList());
    }
}