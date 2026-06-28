package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceHelper {

    public static InputStream get(String path) {
        if (path.startsWith("web/")) {
            return ModLoaderBridgeInstance.get().isProduction() ? getProduction(path) : getDevelopment(path);
        } else {
            return loadFromResource(path);
        }
    }

    public static boolean has(String path) {
        if (path.startsWith("web/")) {
            return ModLoaderBridgeInstance.get().isProduction() ? hasProduction(path) : hasDevelopment(path);
        } else {
            return hasResource(path);
        }
    }

    private static InputStream getDevelopment(String filename) {
        return loadFromFile(getLocalFilePath(filename));
    }

    private static InputStream getProduction(String filename) {
        if (filename.startsWith("web/")) {
            String dir = System.getProperty("cheatutils.web.dir");
            if (dir != null) {
                return loadFromFile(Paths.get(dir, filename.substring(4)));
            }
        }

        return loadFromResource(filename);
    }

    private static boolean hasDevelopment(String filename) {
        return getLocalFilePath(filename).toFile().exists();
    }

    private static boolean hasProduction(String filename) {
        return hasResource(filename);
    }

    private static boolean hasResource(String filename) {
        ClassLoader classLoader = ResourceHelper.class.getClassLoader();
        return classLoader.getResource(filename) != null;
    }

    private static Path getLocalFilePath(String filename) {
        return Paths.get(System.getProperty("user.dir"), "../../common/resources", filename);
    }

    private static InputStream loadFromResource(String filename) {
        ClassLoader classLoader = ResourceHelper.class.getClassLoader();
        return classLoader.getResourceAsStream(filename);
    }

    private static InputStream loadFromFile(Path path) {
        try {
            return new FileInputStream(path.toString());
        } catch (IOException e) {
            return null;
        }
    }
}